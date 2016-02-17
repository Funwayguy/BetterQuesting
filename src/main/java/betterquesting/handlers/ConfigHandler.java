package betterquesting.handlers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;
import betterquesting.core.BQ_Settings;
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
		
		BQ_Settings.hideUpdates = config.getBoolean("Hide Updates", Configuration.CATEGORY_GENERAL, false, "Hide update notifications");
		BQ_Settings.curTheme = config.getString("Theme", Configuration.CATEGORY_GENERAL, "betterquesting:light", "The current questing theme");
		BQ_Settings.noticeComplete = config.getString("Sound Complete", Configuration.CATEGORY_GENERAL, "random.levelup", "Sound that plays when a quest is completed");
		BQ_Settings.noticeUpdate = config.getString("Sound Update", Configuration.CATEGORY_GENERAL, "random.levelup", "Sound that plays when a quest is completed");
		BQ_Settings.noticeUnlock = config.getString("Sound Unlock", Configuration.CATEGORY_GENERAL, "random.click", "Sound that plays when a quest is completed");
		
		String catTitle = "TitleCard";
		BQ_Settings.titleCard = config.getString("Title Texture", catTitle, "betterquesting:textures/gui/default_title.png", "Texture used in the quest tile card");
		BQ_Settings.titleAlignX = config.getFloat("Align X", catTitle, 0.5F, 0F, 1F, "X alignment origin point");
		BQ_Settings.titleAlignY = config.getFloat("Align Y", catTitle, 0F, 0F, 1F, "Y alignment origin point");
		BQ_Settings.titleOffX = config.getInt("Offset X", catTitle, -128, Integer.MIN_VALUE, Integer.MAX_VALUE, "X offset in pixels");
		BQ_Settings.titleOffY = config.getInt("Offset Y", catTitle, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, "Y offset in pixels");
		
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
