package betterquesting.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.QuestEvent;
import betterquesting.api.events.QuestEvent.Type;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.cache.QuestCache.QResetTime;
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
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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
	@OnlyIn(Dist.CLIENT)
	public void onKey(InputEvent.KeyInputEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		
		if(BQ_Keybindings.openQuests.isPressed())
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
	
	@SubscribeEvent
    public void onCapabilityPlayer(AttachCapabilitiesEvent<Entity> event)
    {
        if(!(event.getObject() instanceof PlayerEntity)) return;
        event.addCapability(CapabilityProviderQuestCache.LOC_QUEST_CACHE, new CapabilityProviderQuestCache());
    }
    
    @SubscribeEvent
    public void onPlayerClone(Clone event)
    {
        betterquesting.api2.cache.QuestCache oCache = event.getOriginal().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null).orElse(null);
        betterquesting.api2.cache.QuestCache nCache = event.getPlayer().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null).orElse(null);
        
        if(oCache != null && nCache != null) nCache.deserializeNBT(oCache.serializeNBT());
    }
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if(event.getEntityLiving().world.isRemote) return;
		
		if(event.getEntityLiving() instanceof ServerPlayerEntity)
		{
			if(event.getEntityLiving().ticksExisted%20 != 0) return; // Only triggers once per second
			
			ServerPlayerEntity player = (ServerPlayerEntity)event.getEntityLiving();
            QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null).orElseGet(QuestCache::new);
            boolean editMode = QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE);
            
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
	}
	
	// TODO: Create a new message inbox system for these things. On screen popups aren't ideal in combat
	private static void postPresetNotice(IQuest quest, PlayerEntity player, int preset)
	{
	    if(!(player instanceof ServerPlayerEntity)) return;
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
		
		NetNotices.sendNotice(quest.getProperty(NativeProps.GLOBAL) ? null : new ServerPlayerEntity[]{(ServerPlayerEntity)player}, icon, mainText, subText, sound);
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		if(!event.getWorld().isRemote() && BQ_Settings.curWorldDir != null && event.getWorld().getDimension().getType().getId() == 0)
		{
			SaveLoadHandler.INSTANCE.saveDatabases();
		}
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(event.getPlayer().world.isRemote || event.getPlayer().getServer() == null || !(event.getPlayer() instanceof ServerPlayerEntity)) return;
		
		ServerPlayerEntity mpPlayer = (ServerPlayerEntity)event.getPlayer();
		
		if(BetterQuesting.isClient() && !mpPlayer.getServer().isDedicatedServer() && mpPlayer.getServer().getServerOwner().equals(mpPlayer.getGameProfile().getName()))
        {
            NameCache.INSTANCE.updateName(mpPlayer);
            return;
        }
		
		NetBulkSync.sendReset(mpPlayer, true, true);
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) && event.getPlayer() instanceof ServerPlayerEntity && !((ServerPlayerEntity)event.getPlayer()).queuedEndExit)
		{
			ServerPlayerEntity mpPlayer = (ServerPlayerEntity)event.getPlayer();
			
			int lives = LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(mpPlayer));
			
			if(lives <= 0)
			{
				MinecraftServer server = mpPlayer.getServer();
				if(server == null) return;
	            
	            mpPlayer.setGameType(GameType.SPECTATOR);
				if(!server.isDedicatedServer()) mpPlayer.getServerWorld().getGameRules().get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(false, server);
			} else
			{
				if(lives == 1)
				{
					mpPlayer.sendStatusMessage(new StringTextComponent("This is your last life!"), true);
				} else
				{
					mpPlayer.sendStatusMessage(new StringTextComponent(lives + " lives remaining!"), true);
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
		
		if(event.getEntityLiving() instanceof PlayerEntity)
		{
			UUID uuid = QuestingAPI.getQuestingUUID(((PlayerEntity)event.getEntityLiving()));
			
            int lives = LifeDatabase.INSTANCE.getLives(uuid);
            LifeDatabase.INSTANCE.setLives(uuid, lives - 1);
		}
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event)
	{
		if(event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE))
		{
			//event.getMap().registerSprite(FluidPlaceholder.fluidPlaceholder.getStill());
		}
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onDataUpdated(DatabaseEvent.Update event)
	{
		// TODO: Change this to a proper panel event. Also explain WHAT updated
		final Screen screen = Minecraft.getInstance().currentScreen;
		if(screen instanceof INeedsRefresh) Minecraft.getInstance().deferTask(((INeedsRefresh)screen)::refreshGui);
	}
	
	/*@SubscribeEvent
	public void onCommand(CommandEvent event)
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		String comName = event.getParseResults().getContext().getRootNode().getName();
		ParsedArgument<? ,?> args = event.getParseResults().getContext().getArguments().get("targets");
		
		if(server != null && (comName.equalsIgnoreCase("op") || comName.equalsIgnoreCase("deop")))
		{
		    ServerPlayerEntity playerMP = server.getPlayerList().getPlayerByUsername(event.getParameters()[0]);
			if(playerMP != null) opQueue.add(playerMP); // Has to be delayed until after the event when the command has executed
		}
	}*/
	
	private final ArrayDeque<ServerPlayerEntity> opQueue = new ArrayDeque<>();
	private boolean openToLAN = false;
	
	@SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if(event.phase != Phase.END) return;
        
        if(!openToLAN)
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server.isDedicatedServer())
            {
                openToLAN = true;
            } else if(server.getPublic())
            {
                openToLAN = true;
                opQueue.addAll(server.getPlayerList().getPlayers());
            }
        }
        
        while(!opQueue.isEmpty())
        {
            ServerPlayerEntity playerMP = opQueue.poll();
            if(playerMP != null && NameCache.INSTANCE.updateName(playerMP))
            {
                DBEntry<IParty> party = PartyManager.INSTANCE.getParty(QuestingAPI.getQuestingUUID(playerMP));
                if(party != null)
                {
                    NetNameSync.quickSync(null, party.getID());
                } else
                {
                    NetNameSync.sendNames(new ServerPlayerEntity[]{playerMP}, new UUID[]{QuestingAPI.getQuestingUUID(playerMP)}, null);
                }
            }
        }
    }
}
