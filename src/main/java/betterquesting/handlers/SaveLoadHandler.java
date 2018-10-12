package betterquesting.handlers;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.QuestCache;
import betterquesting.client.QuestNotification;
import betterquesting.client.gui2.GuiHome;
import betterquesting.core.BetterQuesting;
import betterquesting.legacy.ILegacyLoader;
import betterquesting.legacy.LegacyLoaderRegistry;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SaveLoadHandler
{
    public static SaveLoadHandler INSTANCE = new SaveLoadHandler();
    
    private boolean hasUpdate = false;
    
    public boolean hasUpdate()
	{
		return this.hasUpdate;
	}
	
	public void resetUpdate()
	{
		this.hasUpdate = false;
	}
    
    public void loadDatabases(MinecraftServer server)
    {
        QuestSettings.INSTANCE.reset();
		QuestDatabase.INSTANCE.reset();
		QuestLineDatabase.INSTANCE.reset();
		LifeDatabase.INSTANCE.reset();
		NameCache.INSTANCE.reset();
        
        QuestCache.INSTANCE.reset();
        hasUpdate = false;
		
		if(BetterQuesting.proxy.isClient())
		{
			GuiHome.bookmark = null;
			QuestNotification.resetNotices();
		}
		
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
		int packVer = 0;
		String packName = "";
		
		if(useDef) // LOAD DEFAULTS
		{
			fileDatabase = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
		} else
		{
			JsonObject defTmp = JsonHelper.ReadFromFile(new File(BQ_Settings.defaultDir, "DefaultQuests.json"));
			QuestSettings tmpSettings = new QuestSettings();
			tmpSettings.readFromNBT(NBTConverter.JSONtoNBT_Object(defTmp, new NBTTagCompound(), true).getCompoundTag("questSettings"));
			packVer = tmpSettings.getProperty(NativeProps.PACK_VER);
			packName = tmpSettings.getProperty(NativeProps.PACK_NAME);
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
			
			BetterQuesting.logger.warn("BetterQuesting has been updated to from \"" + fsVer + "\" to \"" + cVer + "\"! Creating back ups...");
			
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
			
			if(useDef)
			{
				QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, false); // Force edit off
			}
			
			hasUpdate = packName.equals(QuestSettings.INSTANCE.getProperty(NativeProps.PACK_NAME)) && packVer > QuestSettings.INSTANCE.getProperty(NativeProps.PACK_VER);
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
	    
	    BetterQuesting.logger.info("Loaded " + QuestDatabase.INSTANCE.size() + " quests");
	    BetterQuesting.logger.info("Loaded " + QuestLineDatabase.INSTANCE.size() + " quest lines");
	    BetterQuesting.logger.info("Loaded " + PartyManager.INSTANCE.size() + " parties");
	    BetterQuesting.logger.info("Loaded " + NameCache.INSTANCE.size() + " names");
	    
	    MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Load());
    }
    
    static class AsyncSave implements IThreadedFileIO {

      private final List<AsyncSaveJob> jobs = new ArrayList<>(); 

      void enqueue(File file, JsonObject jObj) {
        jobs.add(new AsyncSaveJob(file, jObj));
      }
      
      void start() {
        ThreadedFileIOBase.getThreadedIOInstance().queueIO(this);
      }

      @Override
      public boolean writeNextIO() {
        if (!jobs.isEmpty()) {
          AsyncSaveJob job = jobs.remove(0);
          JsonHelper.WriteToFile(job.file, job.jObj);
        }
        return !jobs.isEmpty();
      }

    }

    static class AsyncSaveJob  {

      final File file; 
      final JsonObject jObj;

      AsyncSaveJob(File file, JsonObject jObj) {
        this.file = file;
        this.jObj = jObj;
      }

    }
    
    public void saveDatabases()
    {
      
      AsyncSave save = new AsyncSave();
      
        // === CONFIG ===
        
        NBTTagCompound jsonCon = new NBTTagCompound();
        
        jsonCon.setTag("questSettings", QuestSettings.INSTANCE.writeToNBT(new NBTTagCompound()));
        jsonCon.setTag("questDatabase", QuestDatabase.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
        jsonCon.setTag("questLines", QuestLineDatabase.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
        
        jsonCon.setString("format", BetterQuesting.FORMAT);
        jsonCon.setString("build", Loader.instance().activeModContainer().getVersion());
        
        save.enqueue(new File(BQ_Settings.curWorldDir, "QuestDatabase.json"), NBTConverter.NBTtoJSON_Compound(jsonCon, new JsonObject(), true));
        
        // === PROGRESS ===
        
        NBTTagCompound jsonProg = new NBTTagCompound();
        
        jsonProg.setTag("questProgress", QuestDatabase.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.PROGRESS));
        
        save.enqueue(new File(BQ_Settings.curWorldDir, "QuestProgress.json"), NBTConverter.NBTtoJSON_Compound(jsonProg, new JsonObject(), true));
        
        // === PARTIES ===
        
        NBTTagCompound jsonP = new NBTTagCompound();
        
        jsonP.setTag("parties", PartyManager.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
        
        save.enqueue(new File(BQ_Settings.curWorldDir, "QuestingParties.json"), NBTConverter.NBTtoJSON_Compound(jsonP, new JsonObject(), true));
        
        // === NAMES ===
        
        NBTTagCompound jsonN = new NBTTagCompound();
        
        jsonN.setTag("nameCache", NameCache.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
        
        save.enqueue(new File(BQ_Settings.curWorldDir, "NameCache.json"), NBTConverter.NBTtoJSON_Compound(jsonN, new JsonObject(), true));
        
        // === LIVES ===
        
        NBTTagCompound jsonL = new NBTTagCompound();
        
        jsonL.setTag("lifeDatabase", LifeDatabase.INSTANCE.writeToNBT(new NBTTagCompound(), EnumSaveType.PROGRESS));
        
        save.enqueue(new File(BQ_Settings.curWorldDir, "LifeDatabase.json"), NBTConverter.NBTtoJSON_Compound(jsonL, new JsonObject(), true));
        
        save.start();
        
        MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Save());
    }
    
    public void unloadDatabases()
    {
        BQ_Settings.curWorldDir = null;
        hasUpdate = false;
        
        QuestSettings.INSTANCE.reset();
        QuestDatabase.INSTANCE.reset();
        QuestLineDatabase.INSTANCE.reset();
        LifeDatabase.INSTANCE.reset();
        NameCache.INSTANCE.reset();
        
        QuestCache.INSTANCE.reset();
        
        if(BetterQuesting.proxy.isClient())
		{
			GuiHome.bookmark = null;
			QuestNotification.resetNotices();
		}
    }
}
