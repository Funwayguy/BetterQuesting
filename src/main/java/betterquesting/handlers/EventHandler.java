package betterquesting.handlers;

import java.io.File;
import java.util.UUID;

import betterquesting.api2.storage.DBEntry;
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
import net.minecraftforge.fml.common.Loader;
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
import betterquesting.api2.client.gui.GuiScreenTest;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.gui2.GuiHome;
import betterquesting.core.BetterQuesting;
import betterquesting.legacy.ILegacyLoader;
import betterquesting.legacy.LegacyLoaderRegistry;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
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
	public static EventHandler INSTANCE = new EventHandler();
	
	private EventHandler()
	{
		// Singleton
	}
	
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
				if(quest.canSubmit(player)) // Tasks active or repeating
				{
					boolean syncMe = false;
					
					for(DBEntry<ITask> task : quest.getTasks().getEntries())
					{
						if(task.getValue() instanceof ITickableTask && !task.getValue().isComplete(uuid))
						{
							((ITickableTask)task.getValue()).updateTask(player, quest);
							
							if(task.getValue().isComplete(uuid))
							{
								syncMe = true;
							}
						}
					}
				
					if(syncMe)
					{
						quest.update(player);
						
						if(!quest.isComplete(uuid))
						{
							PacketSender.INSTANCE.sendToAll(quest.getSyncPacket());
						} else
						{
							refreshCache = true;
						}
					}
				} else if(quest.isComplete(uuid)) // Complete & inactive
				{
					if(player.ticksExisted % 10 == 0 && quest.getProperties().getProperty(NativeProps.REPEAT_TIME).intValue() >= 0) // Waiting to reset
					{
						quest.update(player); // This will broadcast a sync anyway
					} else
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
			// === CONFIG ===
			
			NBTTagCompound jsonCon = new NBTTagCompound();
			
			jsonCon.setTag("questSettings", QuestSettings.INSTANCE.writeToNBT(new NBTTagCompound()));
			jsonCon.setTag("questDatabase", QuestDatabase.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
			jsonCon.setTag("questLines", QuestLineDatabase.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
			
			jsonCon.setString("format", BetterQuesting.FORMAT);
			jsonCon.setString("build", Loader.instance().activeModContainer().getVersion());
			
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
			GuiHome.bookmark = null;
		}
		
		MinecraftServer server = event.getWorld().getMinecraftServer();
		
		File rootDir;
		
		if(BetterQuesting.proxy.isClient())
		{
			BQ_Settings.curWorldDir = server.getFile("saves/" + server.getFolderName() + "/betterquesting");
			rootDir = server.getFile("saves/" + server.getFolderName());
		} else
		{
			BQ_Settings.curWorldDir = server.getFile(server.getFolderName() + "/betterquesting");
			rootDir = server.getFile(server.getFolderName());
		}
		
		File fileDatabase = new File(BQ_Settings.curWorldDir, "QuestDatabase.json");
		File fileProgress = new File(BQ_Settings.curWorldDir, "QuestProgress.json");
		File fileParties = new File(BQ_Settings.curWorldDir, "QuestingParties.json");
		File fileLives = new File(BQ_Settings.curWorldDir, "LifeDatabase.json");
		File fileNames = new File(BQ_Settings.curWorldDir, "NameCache.json");
  
		// MOVE BQ1 LEGACY FILES
		
		if(new File(rootDir, "QuestDatabase.json").exists() && !fileDatabase.exists())
		{
			File legFileDat = new File(rootDir, "QuestDatabase.json");
			File legFilePro = new File(rootDir, "QuestProgress.json");
			File legFilePar = new File(rootDir, "QuestingParties.json");
			File legFileLiv = new File(rootDir, "LifeDatabase.json");
			File legFileNam = new File(rootDir, "NameCache.json");
			
			JsonHelper.CopyPaste(legFileDat, fileDatabase);
			JsonHelper.CopyPaste(legFilePro, fileProgress);
			JsonHelper.CopyPaste(legFilePar, fileParties);
			JsonHelper.CopyPaste(legFileLiv, fileLives);
			JsonHelper.CopyPaste(legFileNam, fileNames);
			
			legFileDat.delete();
			legFilePro.delete();
			legFilePar.delete();
			legFileLiv.delete();
			legFileNam.delete();
		}
		
		// === CONFIG ===
		
		boolean useDef = !fileDatabase.exists();
		
		if(useDef) // LOAD DEFAULTS
		{
			fileDatabase = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
		}
		
		JsonObject j1 = JsonHelper.ReadFromFile(fileDatabase);
		
		NBTTagCompound nbt1 = NBTConverter.JSONtoNBT_Object(j1, new NBTTagCompound(), true);
		
		String fVer = nbt1.hasKey("format", 8) ? nbt1.getString("format") : "0.0.0";
		String bVer = nbt1.getString("build");
		String cVer = Loader.instance().activeModContainer().getVersion();
		
		if(!cVer.equalsIgnoreCase(bVer) && !useDef) // RUN BACKUPS
		{
			String fsVer = JsonHelper.makeFileNameSafe(bVer);
			
			if(fsVer.length() <= 0)
			{
				fsVer = "pre-251";
			}
			
			BetterQuesting.logger.log(Level.WARN, "BetterQuesting has been updated to from \"" + fsVer + "\" to \"" + cVer + "\"! Creating back ups...");
			
			JsonHelper.CopyPaste(fileDatabase,	new File(BQ_Settings.curWorldDir + "/backup/" + fsVer, "QuestDatabase_backup_" + fsVer + ".json"));
			JsonHelper.CopyPaste(fileProgress,	new File(BQ_Settings.curWorldDir + "/backup/" + fsVer, "QuestProgress_backup_" + fsVer + ".json"));
			JsonHelper.CopyPaste(fileParties,	new File(BQ_Settings.curWorldDir + "/backup/" + fsVer, "QuestingParties_backup_" + fsVer + ".json"));
			JsonHelper.CopyPaste(fileNames,		new File(BQ_Settings.curWorldDir + "/backup/" + fsVer, "NameCache_backup_" + fsVer + ".json"));
			JsonHelper.CopyPaste(fileLives,		new File(BQ_Settings.curWorldDir + "/backup/" + fsVer, "LifeDatabase_backup_" + fsVer + ".json"));
		}
		
		ILegacyLoader loader = LegacyLoaderRegistry.getLoader(fVer);
		
		if(loader == null)
		{
			QuestSettings.INSTANCE.readFromNBT(nbt1.getCompoundTag("questSettings"));
			QuestDatabase.INSTANCE.readFromNBT(nbt1.getTagList("questDatabase", 10), EnumSaveType.CONFIG);
			QuestLineDatabase.INSTANCE.readFromNBT(nbt1.getTagList("questLines", 10), EnumSaveType.CONFIG);
		} else
		{
			loader.readFromJson(j1, EnumSaveType.CONFIG);
		}
    	
		// === PROGRESS ===
		
		JsonObject j2 = JsonHelper.ReadFromFile(fileProgress);
		
		if(loader == null)
		{
			NBTTagCompound nbt2 = NBTConverter.JSONtoNBT_Object(j2, new NBTTagCompound(), true);
			QuestDatabase.INSTANCE.readFromNBT(nbt2.getTagList("questProgress", 10), EnumSaveType.PROGRESS);
		} else
		{
			loader.readFromJson(j2, EnumSaveType.PROGRESS);
		}
		
		// === PARTIES ===
		
	    JsonObject j3 = JsonHelper.ReadFromFile(fileParties);
	    
		NBTTagCompound nbt3 = NBTConverter.JSONtoNBT_Object(j3, new NBTTagCompound(), true);
	    PartyManager.INSTANCE.readFromNBT(nbt3.getTagList("parties", 10), EnumSaveType.CONFIG);
	    
	    // === NAMES ===
	    
	    JsonObject j4 = JsonHelper.ReadFromFile(fileNames);
	    
		NBTTagCompound nbt4 = NBTConverter.JSONtoNBT_Object(j4, new NBTTagCompound(), true);
	    NameCache.INSTANCE.readFromNBT(nbt4.getTagList("nameCache", 10), EnumSaveType.CONFIG);
	    
	    // === LIVES ===
	    
	    JsonObject j5 = JsonHelper.ReadFromFile(fileLives);
	    
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
		if(!event.player.world.isRemote && event.player instanceof EntityPlayerMP)
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
