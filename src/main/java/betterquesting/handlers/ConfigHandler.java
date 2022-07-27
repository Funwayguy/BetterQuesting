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

        BQ_Settings.questNotices = config.getBoolean(
                "Quest Notices",
                Configuration.CATEGORY_GENERAL,
                true,
                "Enabled the popup notices when quests are completed or updated");
        BQ_Settings.curTheme = config.getString(
                "Theme", Configuration.CATEGORY_GENERAL, "betterquesting:light", "The current questing theme");
        BQ_Settings.useBookmark = config.getBoolean(
                "Use Quest Bookmark", Configuration.CATEGORY_GENERAL, true, "Jumps the user to the last opened quest");
        BQ_Settings.guiWidth = config.getInt(
                "Max GUI Width",
                Configuration.CATEGORY_GENERAL,
                -1,
                -1,
                Integer.MAX_VALUE,
                "Clamps the max UI width (-1 to disable)");
        BQ_Settings.guiHeight = config.getInt(
                "Max GUI Height",
                Configuration.CATEGORY_GENERAL,
                -1,
                -1,
                Integer.MAX_VALUE,
                "Clamps the max UI height (-1 to disable)");
        BQ_Settings.textWidthCorrection = config.getFloat(
                "Text Width Correction",
                Configuration.CATEGORY_GENERAL,
                1F,
                0.01F,
                10.0F,
                "Correcting the width of split text");

        BQ_Settings.scrollMultiplier = config.getFloat(
                "Scroll multiplier", Configuration.CATEGORY_GENERAL, 1F, 0F, 10F, "Scrolling multiplier");

        BQ_Settings.zoomSpeed =
                config.getFloat("Zoom Speed", Configuration.CATEGORY_GENERAL, 1.25F, 1.05F, 3F, "Zoom Speed");

        BQ_Settings.zoomTimeInMs = config.getFloat(
                "Zoom smoothness in ms", Configuration.CATEGORY_GENERAL, 100F, 0F, 2000F, "Zoom smoothness in ms");

        BQ_Settings.zoomInToCursor = config.getBoolean(
                "Zoom in on cursor",
                Configuration.CATEGORY_GENERAL,
                true,
                "Zoom in on cursor. If false, zooms in on center of screen.");
        BQ_Settings.zoomOutToCursor = config.getBoolean(
                "Zoom out on cursor",
                Configuration.CATEGORY_GENERAL,
                true,
                "Zoom out on cursor. If false, zooms out on center of screen.");

        BQ_Settings.claimAllConfirmation = config.getBoolean(
                "Claim all requires confirmation",
                Configuration.CATEGORY_GENERAL,
                true,
                "If true, then when you click on Claim all, a warning dialog will be displayed");

        BQ_Settings.skipHome = config.getBoolean(
                "Skip home",
                Configuration.CATEGORY_GENERAL,
                false,
                "If true will skip home gui and open quests at startup. This property will be changed by the mod itself.");

        BQ_Settings.lockTray = config.getBoolean(
                "Lock tray",
                Configuration.CATEGORY_GENERAL,
                false,
                "Is quest chapters list locked and opened on start.");
        BQ_Settings.viewMode = config.getBoolean(
                "View mode",
                Configuration.CATEGORY_GENERAL,
                false,
                "If true, user can view not-yet-unlocked quests that are not hidden or secret. This property can be changed by the GUI.");
        BQ_Settings.viewModeBtn = config.getBoolean(
                "View mode button", Configuration.CATEGORY_GENERAL, false, "If true, show view mode button.");
        BQ_Settings.alwaysDrawImplicit = config.getBoolean(
                "Always draw implicit dependency",
                Configuration.CATEGORY_GENERAL,
                false,
                "If true, always draw implicit dependency. This property can be changed by the GUI");

        config.save();
    }
}
