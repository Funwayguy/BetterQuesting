package betterquesting.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.QuestEvent;
import betterquesting.api.events.QuestEvent.Type;
import betterquesting.api.placeholders.FluidPlaceholder;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache.QResetTime;
import betterquesting.api2.client.gui.GuiScreenTest;
import betterquesting.api2.client.gui.themes.gui_args.GArgsNone;
import betterquesting.api2.client.gui.themes.presets.PresetGUIs;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.gui2.GuiHome;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.handlers.NetBulkSync;
import betterquesting.network.handlers.NetNameSync;
import betterquesting.network.handlers.NetNotices;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.party.PartyInvitations;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Event handling for standard quests and core BetterQuesting functionality
 */
public class EventHandler
{
	public static final EventHandler INSTANCE = new EventHandler();
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKey(InputEvent.KeyInputEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		if(mc.currentScreen == null && BQ_Keybindings.openQuests.isPressed())
		{
			if(mc.player.isSneaking() && mc.player.getName().equalsIgnoreCase("Funwayguy"))
			{
				mc.displayGuiScreen(new GuiScreenTest(mc.currentScreen));
			} else
			{
				if(BQ_Settings.useBookmark && GuiHome.bookmark != null)
				{
					mc.displayGuiScreen(GuiHome.bookmark);
				} else
				{
					mc.displayGuiScreen(ThemeRegistry.INSTANCE.getGui(PresetGUIs.HOME, GArgsNone.NONE));
				}
			}
		}
	}
	
	@SubscribeEvent
    public void onCapabilityPlayer(AttachCapabilitiesEvent<Entity> event)
    {
        if(!(event.getObject() instanceof EntityPlayer)) return;
        event.addCapability(CapabilityProviderQuestCache.LOC_QUEST_CACHE, new CapabilityProviderQuestCache());
    }
    
    @SubscribeEvent
    public void onPlayerClone(Clone event)
    {
        betterquesting.api2.cache.QuestCache oCache = event.getOriginal().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        betterquesting.api2.cache.QuestCache nCache = event.getEntityPlayer().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        
        if(oCache != null && nCache != null) nCache.deserializeNBT(oCache.serializeNBT());
    }
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if(event.getEntityLiving().world.isRemote) return;
		if(!(event.getEntityLiving() instanceof EntityPlayerMP)) return;
        if(event.getEntityLiving().ticksExisted%20 != 0) return; // Only triggers once per second
        
        EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
        betterquesting.api2.cache.QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        boolean editMode = QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE);
        
        if(qc == null) return;
        
        List<DBEntry<IQuest>> activeQuests = QuestDatabase.INSTANCE.bulkLookup(qc.getActiveQuests());
        List<DBEntry<IQuest>> pendingAutoClaims = QuestDatabase.INSTANCE.bulkLookup(qc.getPendingAutoClaims());
        QResetTime[] pendingResets = qc.getScheduledResets();
        
        UUID uuid = QuestingAPI.getQuestingUUID(player);
        boolean refreshCache = false;
        
        if(!editMode && player.ticksExisted%60 == 0) // Passive quest state check every 3 seconds
        {
            List<Integer> com = new ArrayList<>();
            
            for(DBEntry<IQuest> quest : activeQuests)
            {
                if(!quest.getValue().isUnlocked(uuid)) continue; // Although it IS active, it cannot be completed yet
                
                if(quest.getValue().canSubmit(player)) quest.getValue().update(player);
                
                if(quest.getValue().isComplete(uuid) && !quest.getValue().canSubmit(player))
                {
                    refreshCache = true;
                    qc.markQuestDirty(quest.getID());
                    
                    com.add(quest.getID());
                    if(!quest.getValue().getProperty(NativeProps.SILENT)) postPresetNotice(quest.getValue(), player, 2);
                }
            }
            
            MinecraftForge.EVENT_BUS.post(new QuestEvent(Type.COMPLETED, uuid, com));
        }
        
        if(!editMode && player.getServer() != null) // Repeatable quest resets
        {
            List<Integer> res = new ArrayList<>();
            long totalTime = System.currentTimeMillis();
            
            for(QResetTime rTime : pendingResets)
            {
                IQuest entry = QuestDatabase.INSTANCE.getValue(rTime.questID);
                
                if(totalTime >= rTime.time && !entry.canSubmit(player)) // REEEEEEEEEset
                {
                    if(entry.getProperty(NativeProps.GLOBAL))
                    {
                        entry.resetUser(null, false);
                    } else
                    {
                        entry.resetUser(uuid, false);
                    }
                    
                    refreshCache = true;
                    qc.markQuestDirty(rTime.questID);
                    res.add(rTime.questID);
                    if(!entry.getProperty(NativeProps.SILENT)) postPresetNotice(entry, player, 1);
                } else break; // Entries are sorted by time so we fail fast and skip checking the others
            }
            
            MinecraftForge.EVENT_BUS.post(new QuestEvent(Type.RESET, uuid, res));
        }
        
        if(!editMode)
        {
            for(DBEntry<IQuest> entry : pendingAutoClaims) // Auto claims
            {
                if(entry.getValue().canClaim(player))
                {
                    entry.getValue().claimReward(player);
                    refreshCache = true;
                    qc.markQuestDirty(entry.getID());
                    // Not going to notify of auto-claims anymore. Kinda pointless if they're already being pinged for completion
                }
            }
        }
        
        if(refreshCache || player.ticksExisted % 200 == 0) // Refresh the cache if something changed or every 10 seconds
        {
            qc.updateCache(player);
        }
        
        if(qc.getDirtyQuests().length > 0) NetQuestSync.sendSync(player, qc.getDirtyQuests(), false, true);
        qc.cleanAllQuests();
	}
	
	// TODO: Create a new message inbox system for these things. On screen popups aren't ideal in combat
	private static void postPresetNotice(IQuest quest, EntityPlayer player, int preset)
	{
	    if(!(player instanceof EntityPlayerMP)) return;
        ItemStack icon = quest.getProperty(NativeProps.ICON).getBaseStack();
        String mainText = "";
        String subText = quest.getProperty(NativeProps.NAME);
        String sound = "";
	    
		switch(preset)
		{
			case 0:
            {
                mainText = "betterquesting.notice.unlock";
                sound = quest.getProperty(NativeProps.SOUND_UNLOCK);
                break;
            }
			case 1:
            {
                mainText = "betterquesting.notice.update";
                sound = quest.getProperty(NativeProps.SOUND_UPDATE);
                break;
            }
			case 2:
            {
                mainText = "betterquesting.notice.complete";
                sound = quest.getProperty(NativeProps.SOUND_COMPLETE);
                break;
            }
		}
		
		NetNotices.sendNotice(quest.getProperty(NativeProps.GLOBAL) ? null : new EntityPlayerMP[]{(EntityPlayerMP)player}, icon, mainText, subText, sound);
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if(event.getModID().equals(BetterQuesting.MODID))
		{
			ConfigHandler.config.save();
			ConfigHandler.initConfigs();
		}
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		if(!event.getWorld().isRemote && BQ_Settings.curWorldDir != null && event.getWorld().provider.getDimension() == 0)
		{
			SaveLoadHandler.INSTANCE.saveDatabases();
		}
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(event.player.world.isRemote || event.player.getServer() == null || !(event.player instanceof EntityPlayerMP)) return;
		
		EntityPlayerMP mpPlayer = (EntityPlayerMP)event.player;
		
		if(BetterQuesting.proxy.isClient() && !mpPlayer.getServer().isDedicatedServer() && event.player.getServer().getServerOwner().equals(mpPlayer.getGameProfile().getName()))
        {
            NameCache.INSTANCE.updateName(mpPlayer);
            return;
        }
		
		NetBulkSync.sendReset(mpPlayer, true, true);
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) && event.player instanceof EntityPlayerMP && !((EntityPlayerMP)event.player).queuedEndExit)
		{
			EntityPlayerMP mpPlayer = (EntityPlayerMP)event.player;
			
			int lives = LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(mpPlayer));
			
			if(lives <= 0)
			{
				MinecraftServer server = mpPlayer.getServer();
				if(server == null) return;
	            
	            mpPlayer.setGameType(GameType.SPECTATOR);
				if(!server.isDedicatedServer()) mpPlayer.getServerWorld().getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", "false");
			} else
			{
				if(lives == 1)
				{
					mpPlayer.sendStatusMessage(new TextComponentString("This is your last life!"), true);
				} else
				{
					mpPlayer.sendStatusMessage(new TextComponentString(lives + " lives remaining!"), true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(event.getEntityLiving().world.isRemote || !QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE))
		{
			return;
		}
		
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			UUID uuid = QuestingAPI.getQuestingUUID(((EntityPlayer)event.getEntityLiving()));
			
            int lives = LifeDatabase.INSTANCE.getLives(uuid);
            LifeDatabase.INSTANCE.setLives(uuid, lives - 1);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event)
	{
		if(event.getMap() == Minecraft.getMinecraft().getTextureMapBlocks())
		{
			event.getMap().registerSprite(FluidPlaceholder.fluidPlaceholder.getStill());
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onDataUpdated(DatabaseEvent.Update event)
	{
		// TODO: Change this to a proper panel event. Also explain WHAT updated
		final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if(screen instanceof INeedsRefresh) Minecraft.getMinecraft().addScheduledTask(((INeedsRefresh)screen)::refreshGui);
	}
	
	@SubscribeEvent
	public void onCommand(CommandEvent event)
	{
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		
		if(server != null && (event.getCommand().getName().equalsIgnoreCase("op") || event.getCommand().getName().equalsIgnoreCase("deop")))
		{
		    EntityPlayerMP playerMP = server.getPlayerList().getPlayerByUsername(event.getParameters()[0]);
			if(playerMP != null) opQueue.add(playerMP); // Has to be delayed until after the event when the command has executed
		}
	}
	
	private final ArrayDeque<EntityPlayerMP> opQueue = new ArrayDeque<>();
	private boolean openToLAN = false;
	
	@SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if(event.phase != Phase.END) return;
        
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        
        if(!server.isDedicatedServer())
        {
            boolean tmp = openToLAN;
            openToLAN = server instanceof IntegratedServer && ((IntegratedServer)server).getPublic();
            if(openToLAN && !tmp) opQueue.addAll(server.getPlayerList().getPlayers());
        } else if(!openToLAN)
        {
            openToLAN = true;
        }
        
        while(!opQueue.isEmpty())
        {
            EntityPlayerMP playerMP = opQueue.poll();
            if(playerMP != null && NameCache.INSTANCE.updateName(playerMP))
            {
                DBEntry<IParty> party = PartyManager.INSTANCE.getParty(QuestingAPI.getQuestingUUID(playerMP));
                if(party != null)
                {
                    NetNameSync.quickSync(null, party.getID());
                } else
                {
                    NetNameSync.sendNames(new EntityPlayerMP[]{playerMP}, new UUID[]{QuestingAPI.getQuestingUUID(playerMP)}, null);
                }
            }
        }
        
        if(server.getTickCounter() % 60 == 0) PartyInvitations.INSTANCE.cleanExpired();
    }
}
