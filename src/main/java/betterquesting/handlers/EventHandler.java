package betterquesting.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.placeholders.FluidPlaceholder;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.questing.tasks.ITickableTask;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.QuestCache;
import betterquesting.api2.client.gui.GuiScreenTest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.gui2.GuiHome;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
					mc.displayGuiScreen(new GuiHome(mc.currentScreen));
				}
			}
		}
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
			if(QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE))
			{
				return;
			}
			
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			UUID uuid = QuestingAPI.getQuestingUUID(player);
			boolean refreshCache = false;
			
			for(IQuest quest : QuestCache.INSTANCE.getActiveQuests(uuid))
			{
				if(quest.getTasks().size() <= 0 || quest.canSubmit(player)) // Tasks active or repeating
				{
					boolean syncMe = quest.getTasks().size() <= 0;
					boolean wat = true; // Work around for non-tickable tasks
					
					for(DBEntry<ITask> task : quest.getTasks().getEntries())
					{
						if(task.getValue() instanceof ITickableTask && !task.getValue().isComplete(uuid))
						{
							wat = false;
							((ITickableTask)task.getValue()).updateTask(player, quest);
							
							if(task.getValue().isComplete(uuid))
							{
								syncMe = true;
							}
						}
					}
				
					if(syncMe || (wat && player.ticksExisted % 60 == 0))
					{
						quest.update(player);
						
						if(quest.isComplete(uuid))
						{
							refreshCache = true;
						}
					}
					if ((syncMe || player.ticksExisted % 60 == 0) && !quest.isComplete(uuid))
					{
						QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToParty(quest.getProgressSyncPacket(uuid), player);
					}
					
				} else if(quest.isComplete(uuid)) // Complete & inactive
				{
					if(player.ticksExisted % 20 == 0 && (quest.getProperties().getProperty(NativeProps.REPEAT_TIME).intValue() >= 0 || quest.getProperties().getProperty(NativeProps.AUTO_CLAIM))) // Waiting to reset
					{
						quest.update(player); // This will broadcast a sync anyway
					} else
					{
						refreshCache = true;
					}
				} else if(player.ticksExisted % 60 == 0)
				{
					// Quest is in a state where the user can't manually update the tasks but it isn't yet considered complete.
					// Likely an event based task like block break or crafting that doesn't force the quest itself to update
					quest.update(player);
					
					if(quest.isComplete(uuid))
					{
						refreshCache = true;
					}
				}
			}
			
			if(refreshCache || player.ticksExisted % 200 == 0)
			{
				QuestCache.INSTANCE.updateCache(player);
			}
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
		
		PacketSender.INSTANCE.sendToPlayer(NameCache.INSTANCE.getSyncPacket(), mpPlayer);
		NameCache.INSTANCE.updateName(event.player.getServer(), mpPlayer);
		
		UUID uuid = QuestingAPI.getAPI(ApiReference.NAME_CACHE).registerAndGetUUID(mpPlayer);

		PacketSender.INSTANCE.sendToPlayer(QuestSettings.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(QuestDatabase.INSTANCE.getSyncPrivatePacket(uuid), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(QuestLineDatabase.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(LifeDatabase.INSTANCE.getSyncPrivatePacket(uuid), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(PartyManager.INSTANCE.getSyncPacket(), mpPlayer);
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) && event.player instanceof EntityPlayerMP && !((EntityPlayerMP)event.player).queuedEndExit)
		{
			EntityPlayerMP mpPlayer = (EntityPlayerMP)event.player;
			
			IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(mpPlayer));
			int lives = (party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES))? LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(mpPlayer)) : LifeDatabase.INSTANCE.getLives(party);
			
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
			QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToParty(QuestingAPI.getAPI(ApiReference.LIFE_DB).getProgressSyncPacket(uuid), (EntityPlayer) event.getEntityLiving());
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
