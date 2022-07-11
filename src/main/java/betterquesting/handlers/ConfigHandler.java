package betterquesting.handlers;

import betterquesting.api.storage.BQ_Settings;
import betterquesting.core.BetterQuesting;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class ConfigHandler {
    public static Configuration config;

    public static void initConfigs() {
        if (config == null) {
            BetterQuesting.logger.log(Level.ERROR, "Config attempted to be loaded before it was initialised!");
            return;
        }

        config.load();

        BQ_Settings.questNotices = config.getBoolean("Quest Notices", Configuration.CATEGORY_GENERAL, true, "Enabled the popup notices when quests are completed or updated");
        BQ_Settings.curTheme = config.getString("Theme", Configuration.CATEGORY_GENERAL, "betterquesting:light", "The current questing theme");
        BQ_Settings.useBookmark = config.getBoolean("Use Quest Bookmark", Configuration.CATEGORY_GENERAL, true, "Jumps the user to the last opened quest");
        BQ_Settings.guiWidth = config.getInt("Max GUI Width", Configuration.CATEGORY_GENERAL, -1, -1, Integer.MAX_VALUE, "Clamps the max UI width (-1 to disable)");
        BQ_Settings.guiHeight = config.getInt("Max GUI Height", Configuration.CATEGORY_GENERAL, -1, -1, Integer.MAX_VALUE, "Clamps the max UI height (-1 to disable)");

        BQ_Settings.scrollMultiplier = config.getFloat("Scroll Speed Multiplier", Configuration.CATEGORY_GENERAL, 1F, 0F, 10F, "Increases or decreases the scrolling speed");

        BQ_Settings.zoomSpeed = config.getFloat("Zoom Speed", Configuration.CATEGORY_GENERAL, 1.25F, 1.05F, 3F, "Zoom Speed");

        BQ_Settings.zoomTimeInMs = config.getFloat("Zoom Smoothness", Configuration.CATEGORY_GENERAL, 100F, 0F, 2000F, "Zoom smoothness in ms");

        BQ_Settings.zoomInToCursor = config.getBoolean("Zoom In on Cursor", Configuration.CATEGORY_GENERAL, true, "Zoom in on cursor. If false, zooms in on center of screen.");
        BQ_Settings.zoomOutToCursor = config.getBoolean("Zoom Out on Cursor", Configuration.CATEGORY_GENERAL, true, "Zoom out on cursor. If false, zooms out on center of screen.");

        BQ_Settings.claimAllConfirmation = config.getBoolean("Claim all requires confirmation", Configuration.CATEGORY_GENERAL, true, "If true, then when you click on Claim all, a warning dialog will be displayed");
        BQ_Settings.lockTray = config.getBoolean("Lock Tray", Configuration.CATEGORY_GENERAL, false, "If true, locks the quest chapter list and opens it initially");
        BQ_Settings.skipHome = config.getBoolean("Skip Home", Configuration.CATEGORY_GENERAL, false, "If true, skip the home GUI and open quests at startup. This property will be changed by the mod itself.");
        BQ_Settings.viewMode = config.getBoolean("View mode", Configuration.CATEGORY_GENERAL, false, "If view mode enabled, User can view all quests");

        BQ_Settings.defaultVisibility = config.getString("Default Quest Visibility", Configuration.CATEGORY_GENERAL, "NORMAL", "The default visibility value used when creating quests");

        BQ_Settings.spawnWithQuestBook = config.getBoolean("Spawn with Quest Book", Configuration.CATEGORY_GENERAL, true, "If true, then the player will spawn with a Quest Book when they first join the world");
        config.save();
    }
}
