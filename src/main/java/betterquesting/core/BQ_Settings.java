package betterquesting.core;

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
	
	public static boolean useBookmark = true;
	public static String curTheme = "betterquesting:light";
	
	public static String titleCard = "betterquesting:textures/gui/default_title.png";
	public static float titleAlignX = 0.5F;
	public static float titleAlignY = 0F;
	public static int titleOffX = -128;
	public static int titleOffY = 0;
	
	public static boolean hideUpdates = false;
}
