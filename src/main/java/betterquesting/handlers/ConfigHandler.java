package betterquesting.handlers;

import betterquesting.api.storage.BQ_Settings;
import betterquesting.core.BetterQuesting;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

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

		BQ_Settings.claimAllConfirmation = config.getBoolean("Claim all requires confirmation", Configuration.CATEGORY_GENERAL, true, "If true, then when you click on Claim all, a warning dialog will be displayed");
		BQ_Settings.lockTray = config.getBoolean("Lock Tray", Configuration.CATEGORY_GENERAL, false, "If true, locks the quest chapter list and opens it initially");
		config.save();
	}
}
