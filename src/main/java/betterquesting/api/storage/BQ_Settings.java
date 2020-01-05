package betterquesting.api.storage;

import java.io.File;

/**
 * A container for all the configurable settings in the mod
 */
public class BQ_Settings
{
	/**
	 * The root directory of the currently loaded world/save
	 */
	public static File curWorldDir = null;
	public static String defaultDir = "config/betterquesting/";
	
	public static boolean useBookmark;
	public static String curTheme;
	public static int guiWidth;
	public static int guiHeight;
	public static boolean questNotices;
}
