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
		
		BQ_Settings.curTheme = config.getString("Theme", Configuration.CATEGORY_GENERAL, "betterquesting:light", "The current questing theme");
		
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
