package betterquesting.handlers;

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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.placeholders.FluidPlaceholder;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.questing.tasks.ITickableTask;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.QuestCache;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.gui.GuiHome;
import betterquesting.client.gui.GuiQuestLinesMain;
import betterquesting.core.BetterQuesting;
import betterquesting.legacy.ILegacyLoader;
import betterquesting.legacy.LegacyLoaderRegistry;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
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
			if(BQ_Settings.useBookmark && GuiQuestLinesMain.bookmarked != null)
			{
				mc.displayGuiScreen(GuiQuestLinesMain.bookmarked);
			} else
			{
				mc.displayGuiScreen(new GuiHome(mc.currentScreen));
			}
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
			// === CONFIG ===
			
			NBTTagCompound jsonCon = new NBTTagCompound();
			
			jsonCon.setTag("questSettings", QuestSettings.INSTANCE.writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG));
			jsonCon.setTag("questDatabase", QuestDatabase.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
			jsonCon.setTag("questLines", QuestLineDatabase.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
			
			jsonCon.setString("format", BetterQuesting.FORMAT);
			
			JsonHelper.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestDatabase.json"), NBTConverter.NBTtoJSON_Compound(jsonCon, new JsonObject(), true));
			
			// === PROGRESS ===
			
			NBTTagCompound jsonProg = new NBTTagCompound();
			
			jsonProg.setTag("questProgress", QuestDatabase.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.PROGRESS));
			
			JsonHelper.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestProgress.json"), NBTConverter.NBTtoJSON_Compound(jsonProg, new JsonObject(), true));
			
			// === PARTIES ===
			
			NBTTagCompound jsonP = new NBTTagCompound();
			
			jsonP.setTag("parties", PartyManager.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
			
			JsonHelper.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestingParties.json"), NBTConverter.NBTtoJSON_Compound(jsonP, new JsonObject(), true));
			
			// === NAMES ===
			
			NBTTagCompound jsonN = new NBTTagCompound();
			
			jsonN.setTag("nameCache", NameCache.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
			
			JsonHelper.WriteToFile(new File(BQ_Settings.curWorldDir, "NameCache.json"), NBTConverter.NBTtoJSON_Compound(jsonN, new JsonObject(), true));
		    
		    // === LIVES ===
		    
		    NBTTagCompound jsonL = new NBTTagCompound();
		    
		    jsonL.setTag("lifeDatabase", LifeDatabase.INSTANCE.writeToNBT(new NBTTagCompound(), EnumSaveType.PROGRESS));
		    
		    JsonHelper.WriteToFile(new File(BQ_Settings.curWorldDir, "LifeDatabase.json"), NBTConverter.NBTtoJSON_Compound(jsonL, new JsonObject(), true));
		    
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
		
		if(BetterQuesting.proxy.isClient())
		{
			GuiQuestLinesMain.bookmarked = null;
		}
		
		MinecraftServer server =event.getWorld().getMinecraftServer();
		
		File readDir;
		
		if(BetterQuesting.proxy.isClient())
		{
			BQ_Settings.curWorldDir = server.getFile("saves/" + server.getFolderName() + "/betterquesting");
			readDir = server.getFile("saves/" + server.getFolderName());
		} else
		{
			BQ_Settings.curWorldDir = server.getFile(server.getFolderName() + "/betterquesting");
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
		
		NBTTagCompound nbt1 = NBTConverter.JSONtoNBT_Object(j1, new NBTTagCompound(), true);
		
		String fVer = nbt1.hasKey("format", 8) ? nbt1.getString("format") : "0.0.0";
		
		ILegacyLoader loader = LegacyLoaderRegistry.getLoader(fVer);
		
		if(loader == null)
		{
			QuestSettings.INSTANCE.readFromNBT(nbt1.getCompoundTag("questSettings"), EnumSaveType.CONFIG);
			QuestDatabase.INSTANCE.readFromNBT(nbt1.getTagList("questDatabase", 10), EnumSaveType.CONFIG);
			QuestLineDatabase.INSTANCE.readFromNBT(nbt1.getTagList("questLines", 10), EnumSaveType.CONFIG);
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
			NBTTagCompound nbt2 = NBTConverter.JSONtoNBT_Object(j2, new NBTTagCompound(), true);
			QuestDatabase.INSTANCE.readFromNBT(nbt2.getTagList("questProgress", 10), EnumSaveType.PROGRESS);
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
	    
		NBTTagCompound nbt3 = NBTConverter.JSONtoNBT_Object(j3, new NBTTagCompound(), true);
	    PartyManager.INSTANCE.readFromNBT(nbt3.getTagList("parties", 10), EnumSaveType.CONFIG);
	    
	    // === NAMES ===
	    
	    File f4 = new File(BQ_Settings.curWorldDir, "NameCache.json");
	    JsonObject j4 = new JsonObject();
	    
	    if(f4.exists())
	    {
	    	j4 = JsonHelper.ReadFromFile(f4);
	    }
	    
		NBTTagCompound nbt4 = NBTConverter.JSONtoNBT_Object(j4, new NBTTagCompound(), true);
	    NameCache.INSTANCE.readFromNBT(nbt4.getTagList("nameCache", 10), EnumSaveType.CONFIG);
	    
	    // === LIVES ===
	    
	    File f5 = new File(BQ_Settings.curWorldDir, "LifeDatabase.json");
	    JsonObject j5 = new JsonObject();
	    
	    if(f5.exists())
	    {
	    	j5 = JsonHelper.ReadFromFile(f5);
	    }
	    
		NBTTagCompound nbt5 = NBTConverter.JSONtoNBT_Object(j5, new NBTTagCompound(), true);
	    LifeDatabase.INSTANCE.readFromNBT(nbt5.getCompoundTag("lifeDatabase"), EnumSaveType.PROGRESS);
	    
	    BetterQuesting.logger.log(Level.INFO, "Loaded " + QuestDatabase.INSTANCE.size() + " quests");
	    BetterQuesting.logger.log(Level.INFO, "Loaded " + QuestLineDatabase.INSTANCE.size() + " quest lines");
	    BetterQuesting.logger.log(Level.INFO, "Loaded " + PartyManager.INSTANCE.size() + " parties");
	    BetterQuesting.logger.log(Level.INFO, "Loaded " + NameCache.INSTANCE.size() + " names");
	    
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
