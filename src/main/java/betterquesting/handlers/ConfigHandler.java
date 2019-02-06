package betterquesting.handlers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.core.BetterQuesting;

public class ConfigHandler
{
	public static Configuration config;
	
	public static void initConfigs()
	{
		if(config == null)
		{
			BetterQuesting.logger.log(Level.ERROR, "Config attempted to be loaded before it was initialised!");
			return;
		}
		
		config.load();
		
        BQ_Settings.questNotices = config.getBoolean("Quest Notices", Configuration.CATEGORY_GENERAL, true, "Enabled the popup notices when quests are completed or updated");
		BQ_Settings.curTheme = config.getString("Theme", Configuration.CATEGORY_GENERAL, "betterquesting:light", "The current questing theme");
		BQ_Settings.useBookmark = config.getBoolean("Use Quest Bookmark", Configuration.CATEGORY_GENERAL, true, "Jumps the user to the last opened quest");
		BQ_Settings.guiWidth = config.getInt("Max GUI Width", Configuration.CATEGORY_GENERAL, -1, -1, Integer.MAX_VALUE, "Clamps the max UI width (-1 to disable)");
		BQ_Settings.guiHeight = config.getInt("Max GUI Height", Configuration.CATEGORY_GENERAL, -1, -1, Integer.MAX_VALUE, "Clamps the max UI height (-1 to disable)");
		BQ_Settings.dirtyMode = config.getBoolean("Experimental Dirty Mode", Configuration.CATEGORY_GENERAL, true, "Use the experimental system that only saves the database in edit mode or when modified.)");
		
		config.save();
		
		BetterQuesting.logger.log(Level.INFO, "Loaded configs...");
	}
	
	/**
	 * Returns a compound tag representing the configuration settings that need to be synchronized between server and client
	 * @return
	 */
	public static NBTTagCompound getServerConfigs()
	{
		NBTTagCompound tags = new NBTTagCompound();
		
		return tags;
	}
	
	public static void setServerConfigs(NBTTagCompound tags)
	{
	}
}
