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
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.cache.QuestCache.QResetTime;
import betterquesting.api2.client.gui.GuiScreenTest;
import betterquesting.api2.client.gui.themes.gui_args.GArgsNone;
import betterquesting.api2.client.gui.themes.presets.PresetGUIs;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.gui2.GuiHome;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.commons.lang3.Validate;

import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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
			if(mc.thePlayer.isSneaking() && mc.thePlayer.getCommandSenderName().equalsIgnoreCase("Funwayguy"))
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
    public void onEntityJoin(EntityJoinWorldEvent event)
    {
        if(event.entity instanceof EntityPlayer && event.entity.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString()) == null)
        {
            event.entity.registerExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString(), new QuestCache());
        }
    }
    
    @SubscribeEvent
    public void onPlayerClone(Clone event)
    {
        betterquesting.api2.cache.QuestCache oCache = (QuestCache)event.original.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
        betterquesting.api2.cache.QuestCache nCache = (QuestCache)event.entityPlayer.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
        
        if(oCache != null && nCache != null)
        {
            NBTTagCompound tmp = new NBTTagCompound();
            oCache.saveNBTData(tmp);
            nCache.loadNBTData(tmp);
        }
    }
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if(event.entityLiving.worldObj.isRemote)
		{
			return;
		}
		
		if(event.entityLiving instanceof EntityPlayer)
		{
			if(event.entityLiving.ticksExisted%20 != 0) return; // Only triggers once per second
			
			EntityPlayer player = (EntityPlayer)event.entityLiving;
            betterquesting.api2.cache.QuestCache qc = (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
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

                        // Force detect (update) all party members quests
						IParty party = PartyManager.INSTANCE.getUserParty(uuid);
						if(party != null && MinecraftServer.getServer() != null)
						{
							for(UUID memID : party.getMembers()){
								EntityPlayerMP memPlayer = MinecraftServer.getServer().getConfigurationManager().func_152612_a(NameCache.INSTANCE.getName(memID));
								if (memPlayer != null)
									quest.getValue().detect(memPlayer);
							}
						}
                    }
                }
            }
            
            if(!editMode && MinecraftServer.getServer() != null) // Repeatable quest resets
            {
                long totalTime = MinecraftServer.getServer().worldServerForDimension(0).getTotalWorldTime();
                
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
                    
                    if(party != null && MinecraftServer.getServer() != null)
                    {
                        for(UUID memID : party.getMembers()) // Send to party only
                        {
                            EntityPlayerMP memPlayer = MinecraftServer.getServer().getConfigurationManager().func_152612_a(NameCache.INSTANCE.getName(memID));
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
		if(event.modID.equals(BetterQuesting.MODID))
		{
			ConfigHandler.config.save();
			ConfigHandler.initConfigs();
		}
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		if(!event.world.isRemote && BQ_Settings.curWorldDir != null && event.world.provider.dimensionId == 0)
		{
			SaveLoadHandler.INSTANCE.saveDatabases();
		}
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(event.player.worldObj.isRemote || MinecraftServer.getServer() == null || !(event.player instanceof EntityPlayerMP))
		{
			return;
		} else if(BetterQuesting.proxy.isClient() && !MinecraftServer.getServer().isDedicatedServer() && MinecraftServer.getServer().getServerOwner().equals(event.player.getGameProfile().getName()))
		{
			return;
		}
		
		EntityPlayerMP mpPlayer = (EntityPlayerMP)event.player;
		
		NameCache.INSTANCE.updateNames(MinecraftServer.getServer());
		
		PacketSender.INSTANCE.sendToPlayer(QuestSettings.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(QuestDatabase.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(QuestLineDatabase.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(LifeDatabase.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(PartyManager.INSTANCE.getSyncPacket(), mpPlayer);
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) && event.player instanceof EntityPlayerMP && !((EntityPlayerMP)event.player).playerConqueredTheEnd)
		{
			EntityPlayerMP mpPlayer = (EntityPlayerMP)event.player;
			
			IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(mpPlayer));
			int lives = (party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES)) ? LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(mpPlayer)) : LifeDatabase.INSTANCE.getLives(party);
			
			if(lives <= 0)
			{
				MinecraftServer server = MinecraftServer.getServer();
				
				if(server == null)
				{
					return;
				}
	            
	            if (server.isSinglePlayer() && mpPlayer.getCommandSenderName().equals(server.getServerOwner()))
                {
                    mpPlayer.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                    server.deleteWorldAndStopServer();
                }
                else
                {
                    UserListBansEntry userlistbansentry = new UserListBansEntry(mpPlayer.getGameProfile(), null, "(You just lost the game)", null, "Death in Hardcore");
                    server.getConfigurationManager().func_152608_h().func_152687_a(userlistbansentry);
                    mpPlayer.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                }
			} else
			{
				if(lives == 1)
				{
					mpPlayer.addChatComponentMessage(new ChatComponentText("This is your last life!"));
				} else
				{
					mpPlayer.addChatComponentMessage(new ChatComponentText(lives + " lives remaining!"));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(event.entityLiving.worldObj.isRemote || !QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE))
		{
			return;
		}
		
		if(event.entityLiving instanceof EntityPlayer)
		{
			UUID uuid = QuestingAPI.getQuestingUUID(((EntityPlayer)event.entityLiving));
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
		if(event.map.getTextureType() == 0)
		{
            IIcon icon = event.map.registerIcon("betterquesting:fluid_placeholder");
            FluidPlaceholder.fluidPlaceholder.setIcons(icon);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onDataUpdated(DatabaseEvent.Update event)
	{
		// TODO: Change this to a proper panel event. Also explain WHAT updated
		final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if(screen instanceof INeedsRefresh) Minecraft.getMinecraft().func_152343_a(Executors.callable(((INeedsRefresh)screen)::refreshGui));
	}
	
	@SubscribeEvent
	public void onCommand(CommandEvent event)
	{
		MinecraftServer server = MinecraftServer.getServer();
		
		if(server != null && (event.command.getCommandName().equalsIgnoreCase("op") || event.command.getCommandName().equalsIgnoreCase("deop")))
		{
			NameCache.INSTANCE.updateNames(server);
		}
	}
	
	private static final ArrayDeque<FutureTask> serverTasks = new ArrayDeque<>();
	private static Thread serverThread = null;
	
	@SuppressWarnings("UnstableApiUsage")
    public static <T> ListenableFuture<T> scheduleServerTask(Callable<T> task)
    {
        Validate.notNull(task);

        if (Thread.currentThread() != serverThread)
        {
            ListenableFutureTask<T> listenablefuturetask = ListenableFutureTask.create(task);

            synchronized (serverTasks)
            {
                serverTasks.add(listenablefuturetask);
                return listenablefuturetask;
            }
        }
        else
        {
            try
            {
                return Futures.immediateFuture(task.call());
            }
            catch (Exception exception)
            {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        }
    }
	
	@SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if(event.phase != Phase.START) return;
        if(serverThread == null) serverThread = Thread.currentThread();
        
        synchronized(serverTasks)
        {
            while(!serverTasks.isEmpty()) serverTasks.poll().run();
        }
    }
}
