package betterquesting.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.QuestEvent.QuestComplete;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.placeholders.FluidPlaceholder;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache.QResetTime;
import betterquesting.api2.client.gui.GuiScreenTest;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.gui2.GuiHome;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		
		if(BQ_Keybindings.openQuests.isPressed())
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
					mc.displayGuiScreen(ThemeRegistry.INSTANCE.getGuiHook().getHomeScreen(null));
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
		if(event.getEntityLiving().world.isRemote)
		{
			return;
		}
		
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			if(event.getEntityLiving().ticksExisted%20 != 0) return; // Only triggers once per second
			
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
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
                for(DBEntry<IQuest> quest : activeQuests)
                {
                    if(!quest.getValue().isUnlocked(uuid)) continue; // Although it IS active, it cannot be completed yet
                    
                    if(quest.getValue().canSubmit(player)) quest.getValue().update(player);
                    
                    if(quest.getValue().isComplete(uuid) && !quest.getValue().canSubmit(player))
                    {
                        refreshCache = true;
                        qc.markQuestDirty(quest.getID());
                        
                        MinecraftForge.EVENT_BUS.post(new QuestComplete(quest.getID(), uuid));
                        
                        if(!quest.getValue().getProperty(NativeProps.SILENT)) postPresetNotice(quest.getValue(), player, 2);
                    }
                }
            }
            
            if(!editMode && player.getServer() != null) // Repeatable quest resets
            {
                long totalTime = player.getServer().getWorld(0).getTotalWorldTime();
                
                for(QResetTime rTime : pendingResets)
                {
                    IQuest entry = QuestDatabase.INSTANCE.getValue(rTime.questID);
                    
                    if(totalTime >= rTime.time && !entry.canSubmit(player)) // REEEEEEEEEset
                    {
                        if(entry.getProperty(NativeProps.GLOBAL))
                        {
                            entry.resetAll(false);
                        } else
                        {
                            entry.resetUser(uuid, false);
                        }
                        
                        refreshCache = true;
                        qc.markQuestDirty(rTime.questID);
                        if(!entry.getProperty(NativeProps.SILENT)) postPresetNotice(entry, player, 1);
                    } else break; // Entries are sorted by time so we fail fast and skip checking the others
                }
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
            
            List<DBEntry<IQuest>> syncMe = QuestDatabase.INSTANCE.bulkLookup(qc.getDirtyQuests());
            
            // TODO: Check partial data writes from here when fully implemented
            for(DBEntry<IQuest> entry : syncMe)
            {
                if(entry.getValue().getProperty(NativeProps.GLOBAL))
                {
                    PacketSender.INSTANCE.sendToAll(entry.getValue().getSyncPacket());
                } else if(player instanceof EntityPlayerMP)
                {
                    IParty party = PartyManager.INSTANCE.getUserParty(uuid);
                    
                    if(party != null && player.getServer() != null)
                    {
                        for(UUID memID : party.getMembers()) // Send to party only
                        {
                            EntityPlayerMP memPlayer = player.getServer().getPlayerList().getPlayerByUUID(memID);
                            //noinspection ConstantConditions // No idea why IntelliJ is being silly with this null check
                            if(memPlayer != null) PacketSender.INSTANCE.sendToPlayer(entry.getValue().getSyncPacket(), memPlayer);
                        }
                    } else
                    {
                        PacketSender.INSTANCE.sendToPlayer(entry.getValue().getSyncPacket(), (EntityPlayerMP)player);
                    }
                }
            }
            
            qc.cleanAllQuests();
		}
	}
	
	private static void postPresetNotice(IQuest quest, EntityPlayer player, int preset)
	{
		switch(preset)
		{
			case 0:
				postNotice(quest, player, "betterquesting.notice.unlock", quest.getProperty(NativeProps.NAME), quest.getProperty(NativeProps.SOUND_UNLOCK), quest.getProperty(NativeProps.ICON));
				break;
			case 1:
				postNotice(quest, player, "betterquesting.notice.update", quest.getProperty(NativeProps.NAME), quest.getProperty(NativeProps.SOUND_UPDATE), quest.getProperty(NativeProps.ICON));
				break;
			case 2:
				postNotice(quest, player, "betterquesting.notice.complete", quest.getProperty(NativeProps.NAME), quest.getProperty(NativeProps.SOUND_COMPLETE), quest.getProperty(NativeProps.ICON));
				break;
		}
	}
	
	private static void postNotice(IQuest quest, EntityPlayer player, String mainTxt, String subTxt, String sound, BigItemStack icon)
	{
		if(QuestDatabase.INSTANCE.getID(quest) < 0)
		{
			BetterQuesting.logger.error("Non-existant quest is posting notifications!", new Exception());
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		tags.setString("Main", mainTxt);
		tags.setString("Sub", subTxt);
		tags.setString("Sound", sound);
		tags.setTag("Icon", icon.writeToNBT(new NBTTagCompound()));
		QuestingPacket payload = new QuestingPacket(PacketTypeNative.NOTIFICATION.GetLocation(), tags);
		
		if(quest.getProperty(NativeProps.GLOBAL))
		{
			PacketSender.INSTANCE.sendToAll(payload);
		} else if(player instanceof EntityPlayerMP)
		{
		    PacketSender.INSTANCE.sendToPlayer(payload, (EntityPlayerMP)player);
		}
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
		if(event.player.world.isRemote || event.player.getServer() == null || !(event.player instanceof EntityPlayerMP))
		{
			return;
		} else if(BetterQuesting.proxy.isClient() && !event.player.getServer().isDedicatedServer() && event.player.getServer().getServerOwner().equals(event.player.getGameProfile().getName()))
		{
			return;
		}
		
		EntityPlayerMP mpPlayer = (EntityPlayerMP)event.player;
		
		NameCache.INSTANCE.updateNames(event.player.getServer());
		
		PacketSender.INSTANCE.sendToPlayer(QuestSettings.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(QuestDatabase.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(QuestLineDatabase.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(LifeDatabase.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(PartyManager.INSTANCE.getSyncPacket(), mpPlayer);
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) && event.player instanceof EntityPlayerMP && !((EntityPlayerMP)event.player).queuedEndExit)
		{
			EntityPlayerMP mpPlayer = (EntityPlayerMP)event.player;
			
			IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(mpPlayer));
			int lives = (party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES)) ? LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(mpPlayer)) : LifeDatabase.INSTANCE.getLives(party);
			
			if(lives <= 0)
			{
				MinecraftServer server = mpPlayer.getServer();
				
				if(server == null)
				{
					return;
				}
	            
	            mpPlayer.setGameType(GameType.SPECTATOR);
	            mpPlayer.getServerWorld().getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", "false");
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
			IParty party = PartyManager.INSTANCE.getUserParty(uuid);
			
			if(party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES))
			{
				int lives = LifeDatabase.INSTANCE.getLives(uuid);
				LifeDatabase.INSTANCE.setLives(uuid, lives - 1);
			} else
			{
				int lives = LifeDatabase.INSTANCE.getLives(party);
				LifeDatabase.INSTANCE.setLives(party, lives - 1);
			}
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
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		
		// TODO: Change this to a proper panel event. Also explain WHAT updated
        // TODO: This NEEDS to be thread safe. In rare cases the game WILL crash!
		if(screen instanceof INeedsRefresh)
		{
			((INeedsRefresh)screen).refreshGui();
		}
	}
	
	@SubscribeEvent
	public void onCommand(CommandEvent event)
	{
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		
		if(server != null && (event.getCommand().getName().equalsIgnoreCase("op") || event.getCommand().getName().equalsIgnoreCase("deop")))
		{
			NameCache.INSTANCE.updateNames(server);
		}
	}
}
