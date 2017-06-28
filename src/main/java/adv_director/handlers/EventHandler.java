package adv_director.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
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
import org.apache.logging.log4j.Level;
import org.lwjgl.util.vector.Vector4f;
import adv_director.api.api.QuestingAPI;
import adv_director.api.client.gui.misc.INeedsRefresh;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.events.DatabaseEvent;
import adv_director.api.placeholders.FluidPlaceholder;
import adv_director.api.properties.NativeProps;
import adv_director.api.questing.IQuest;
import adv_director.api.questing.party.IParty;
import adv_director.api.questing.tasks.ITask;
import adv_director.api.questing.tasks.ITickableTask;
import adv_director.api.storage.BQ_Settings;
import adv_director.api.utils.JsonHelper;
import adv_director.api.utils.QuestCache;
import adv_director.client.BQ_Keybindings;
import adv_director.core.AdvDirector;
import adv_director.legacy.ILegacyLoader;
import adv_director.legacy.LegacyLoaderRegistry;
import adv_director.network.PacketSender;
import adv_director.questing.QuestDatabase;
import adv_director.questing.QuestInstance;
import adv_director.questing.QuestLineDatabase;
import adv_director.questing.party.PartyManager;
import adv_director.rw2.api.client.gui.GuiScreenTest;
import adv_director.rw2.api.client.gui.misc.GuiPadding;
import adv_director.rw2.api.client.gui.misc.GuiTransform;
import adv_director.storage.LifeDatabase;
import adv_director.storage.NameCache;
import adv_director.storage.QuestSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Event handling for standard quests and core BetterQuesting functionality
 */
public class EventHandler
{
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKey(InputEvent.KeyInputEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		if(BQ_Keybindings.openQuests.isPressed())
		{
			mc.displayGuiScreen(new GuiScreenTest(mc.currentScreen, new GuiTransform(new Vector4f(0F, 0F, 1F, 1F), new GuiPadding(0, 0, 0, 0), 0)));
			
			/*if(BQ_Settings.useBookmark && GuiQuestLinesMain.bookmarked != null)
			{
				mc.displayGuiScreen(GuiQuestLinesMain.bookmarked);
			} else
			{
				mc.displayGuiScreen(new GuiHome(mc.currentScreen));
			}*/
		}
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if(event.getEntityLiving().worldObj.isRemote)
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
			
			List<IQuest> syncList = new ArrayList<IQuest>();
			List<QuestInstance> updateList = new ArrayList<QuestInstance>();
			
			for(Entry<ITask,IQuest> entry : QuestCache.INSTANCE.getActiveTasks(uuid).entrySet())
			{
				ITask task = entry.getKey();
				IQuest quest = entry.getValue();
				
				if(!task.isComplete(uuid))
				{
					task.update(player, quest); // Legacy support only. Will be replaced by ITickableTask
					
					if(task instanceof ITickableTask)
					{
						((ITickableTask)task).updateTask(player, quest);
					}
					
					if(task.isComplete(uuid))
					{
						if(!syncList.contains(quest))
						{
							syncList.add(quest);
						}
						
						if(!updateList.contains(quest) && quest instanceof QuestInstance)
						{
							updateList.add((QuestInstance)quest);
						}
					}
				}
			}
			
			if(player.ticksExisted%20 == 0)
			{
				for(IQuest quest : QuestCache.INSTANCE.getActiveQuests(uuid))
				{
					quest.update(player);
					
					if(quest.isComplete(uuid) && !syncList.contains(quest))
					{
						syncList.add(quest);
						updateList.remove(quest);
					}
				}
				
				QuestCache.INSTANCE.updateCache(player);
			} else
			{
				Iterator<IQuest> iterator = syncList.iterator();
				
				while(iterator.hasNext())
				{
					IQuest quest = iterator.next();
					
					quest.update(player);
					
					if(quest.isComplete(uuid) && !quest.canSubmit(player))
					{
						iterator.remove();
						updateList.remove(quest);
					}
				}
			}
			
			for(IQuest quest : syncList)
			{
				PacketSender.INSTANCE.sendToAll(quest.getSyncPacket());
			}
			
			for(QuestInstance quest : updateList)
			{
				quest.postPresetNotice(player, 1);
			}
		}
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if(event.getModID().equals(AdvDirector.MODID))
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
			// === CONFIG ===
			
			JsonObject jsonCon = new JsonObject();
			
			jsonCon.add("questSettings", QuestSettings.INSTANCE.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			jsonCon.add("questDatabase", QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			jsonCon.add("questLines", QuestLineDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			
			jsonCon.addProperty("format", AdvDirector.FORMAT);
			
			JsonHelper.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestDatabase.json"), jsonCon);
			
			// === PROGRESS ===
			
			JsonObject jsonProg = new JsonObject();
			
			jsonProg.add("questProgress", QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.PROGRESS));
			
			JsonHelper.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestProgress.json"), jsonProg);
			
			// === PARTIES ===
			
			JsonObject jsonP = new JsonObject();
			
			jsonP.add("parties", PartyManager.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			
			JsonHelper.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestingParties.json"), jsonP);
			
			// === NAMES ===
			
			JsonObject jsonN = new JsonObject();
			
			jsonN.add("nameCache", NameCache.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			
			JsonHelper.WriteToFile(new File(BQ_Settings.curWorldDir, "NameCache.json"), jsonN);
		    
		    // === LIVES ===
		    
		    JsonObject jsonL = new JsonObject();
		    
		    jsonL.add("lifeDatabase", LifeDatabase.INSTANCE.writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
		    
		    JsonHelper.WriteToFile(new File(BQ_Settings.curWorldDir, "LifeDatabase.json"), jsonL);
		    
		    MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Save());
		}
	}
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		if(!event.getWorld().isRemote && !event.getWorld().getMinecraftServer().isServerRunning())
		{
			BQ_Settings.curWorldDir = null;
			
			QuestSettings.INSTANCE.reset();
			QuestDatabase.INSTANCE.reset();
			QuestLineDatabase.INSTANCE.reset();
			LifeDatabase.INSTANCE.reset();
			NameCache.INSTANCE.reset();
			
			QuestCache.INSTANCE.reset();
		}
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event)
	{
		if(event.getWorld().isRemote || BQ_Settings.curWorldDir != null)
		{
			return;
		}
		
		QuestSettings.INSTANCE.reset();
		QuestDatabase.INSTANCE.reset();
		QuestLineDatabase.INSTANCE.reset();
		LifeDatabase.INSTANCE.reset();
		NameCache.INSTANCE.reset();
		
		MinecraftServer server =event.getWorld().getMinecraftServer();
		
		File readDir;
		
		if(AdvDirector.proxy.isClient())
		{
			BQ_Settings.curWorldDir = server.getFile("saves/" + server.getFolderName() + "/" + AdvDirector.MODID);
			readDir = server.getFile("saves/" + server.getFolderName());
		} else
		{
			BQ_Settings.curWorldDir = server.getFile(server.getFolderName() + "/" + AdvDirector.MODID);
			readDir = server.getFile(server.getFolderName());
		}
    	
		// Workaround for old files
		boolean rename = false;
		File legFile = new File(readDir, "QuestDatabase.json");
		if(legFile.exists())
		{
			rename = true;
		} else
		{
			readDir = BQ_Settings.curWorldDir;
		}
		
		// === CONFIG ===
		
    	File f1 = new File(readDir, "QuestDatabase.json");
		JsonObject j1 = new JsonObject();
		
		if(f1.exists())
		{
			j1 = JsonHelper.ReadFromFile(f1);
			
			if(rename)
			{
				JsonHelper.CopyPaste(f1, new File(readDir, "QuestDatabase_Legacy.json"));
				f1.delete();
			}
		} else
		{
			f1 = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
			
			if(f1.exists())
			{
				j1 = JsonHelper.ReadFromFile(f1);
			}
		}
		
		String fVer = JsonHelper.GetString(j1, "format", "0.0.0");
		
		ILegacyLoader loader = LegacyLoaderRegistry.getLoader(fVer);
		
		if(loader == null)
		{
			QuestSettings.INSTANCE.readFromJson(JsonHelper.GetObject(j1, "questSettings"), EnumSaveType.CONFIG);
			QuestDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j1, "questDatabase"), EnumSaveType.CONFIG);
			QuestLineDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j1, "questLines"), EnumSaveType.CONFIG);
		} else
		{
			loader.readFromJson(j1, EnumSaveType.CONFIG);
		}
    	
		// === PROGRESS ===
		
    	File f2 = new File(readDir, "QuestProgress.json");
		JsonObject j2 = new JsonObject();
		
		if(f2.exists())
		{
			j2 = JsonHelper.ReadFromFile(f2);
			
			if(rename)
			{
				JsonHelper.CopyPaste(f2, new File(readDir, "QuestDatabase_Legacy.json"));
				f2.delete();
			}
		}
		
		if(loader == null)
		{
			QuestDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j2, "questProgress"), EnumSaveType.PROGRESS);
		} else
		{
			loader.readFromJson(j2, EnumSaveType.PROGRESS);
		}
		
		// === PARTIES ===
		
	    File f3 = new File(BQ_Settings.curWorldDir, "QuestingParties.json");
	    JsonObject j3 = new JsonObject();
	    
	    if(f3.exists())
	    {
	    	j3 = JsonHelper.ReadFromFile(f3);
	    }
	    
	    PartyManager.INSTANCE.readFromJson(JsonHelper.GetArray(j3, "parties"), EnumSaveType.CONFIG);
	    
	    // === NAMES ===
	    
	    File f4 = new File(BQ_Settings.curWorldDir, "NameCache.json");
	    JsonObject j4 = new JsonObject();
	    
	    if(f4.exists())
	    {
	    	j4 = JsonHelper.ReadFromFile(f4);
	    }
	    
	    NameCache.INSTANCE.readFromJson(JsonHelper.GetArray(j4, "nameCache"), EnumSaveType.CONFIG);
	    
	    // === LIVES ===
	    
	    File f5 = new File(BQ_Settings.curWorldDir, "LifeDatabase.json");
	    JsonObject j5 = new JsonObject();
	    
	    if(f5.exists())
	    {
	    	j5 = JsonHelper.ReadFromFile(f5);
	    }
	    
	    LifeDatabase.INSTANCE.readFromJson(JsonHelper.GetObject(j5, "lifeDatabase"), EnumSaveType.CONFIG);
	    LifeDatabase.INSTANCE.readFromJson(JsonHelper.GetObject(j5, "lifeDatabase"), EnumSaveType.PROGRESS);
	    
	    AdvDirector.logger.log(Level.INFO, "Loaded " + QuestDatabase.INSTANCE.size() + " quests");
	    AdvDirector.logger.log(Level.INFO, "Loaded " + QuestLineDatabase.INSTANCE.size() + " quest lines");
	    AdvDirector.logger.log(Level.INFO, "Loaded " + PartyManager.INSTANCE.size() + " parties");
	    AdvDirector.logger.log(Level.INFO, "Loaded " + NameCache.INSTANCE.size() + " names");
	    
	    MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Load());
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(!event.player.worldObj.isRemote && event.player instanceof EntityPlayerMP)
		{
			EntityPlayerMP mpPlayer = (EntityPlayerMP)event.player;
			
			NameCache.INSTANCE.updateNames(event.player.getServer());
			
			PacketSender.INSTANCE.sendToPlayer(QuestSettings.INSTANCE.getSyncPacket(), mpPlayer);
			PacketSender.INSTANCE.sendToPlayer(QuestDatabase.INSTANCE.getSyncPacket(), mpPlayer);
			PacketSender.INSTANCE.sendToPlayer(QuestLineDatabase.INSTANCE.getSyncPacket(), mpPlayer);
			PacketSender.INSTANCE.sendToPlayer(LifeDatabase.INSTANCE.getSyncPacket(), mpPlayer);
			PacketSender.INSTANCE.sendToPlayer(PartyManager.INSTANCE.getSyncPacket(), mpPlayer);
		}
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) && event.player instanceof EntityPlayerMP && !((EntityPlayerMP)event.player).playerConqueredTheEnd)
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
					mpPlayer.addChatComponentMessage(new TextComponentString("This is your last life!"));
				} else
				{
					mpPlayer.addChatComponentMessage(new TextComponentString(lives + " lives remaining!"));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(event.getEntityLiving().worldObj.isRemote || !QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE))
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
		
		if(screen instanceof INeedsRefresh)
		{
			((INeedsRefresh)screen).refreshGui();
		}
	}
	
	@SubscribeEvent
	public void onCommand(CommandEvent event)
	{
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		
		if(server != null && (event.getCommand().getCommandName().equalsIgnoreCase("op") || event.getCommand().getCommandName().equalsIgnoreCase("deop")))
		{
			NameCache.INSTANCE.updateNames(server);
		}
	}
}
