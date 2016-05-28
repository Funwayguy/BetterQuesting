package betterquesting.client.themes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;
import betterquesting.core.BQ_Settings;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.ConfigHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ThemeRegistry
{
	/**
	 * In the event the player's theme is invalid or no longer available, the UI will use this one instead (DO NOT REGISTER THIS THEME)
	 */
	static ThemeBase fallbackTheme = new ThemeStandard("Standard", new ResourceLocation("betterquesting", "textures/gui/editor_gui.png"));
	static HashMap<String,ThemeBase> themeList = new HashMap<String,ThemeBase>();
	
	public static void RegisterThemeManual(ThemeBase theme, String domain, String idName)
	{
		try
		{
			if(idName.contains(":"))
			{
				throw new IllegalArgumentException("Illegal character(s) used in theme ID name");
			}
			
			if(theme == null)
			{
				throw new NullPointerException("Tried to register null theme");
			}
			
			String fullName = domain + ":" + idName;
			
			if(themeList.containsKey(fullName) || themeList.containsValue(theme))
			{
				throw new IllegalArgumentException("Cannot register dupliate theme '" + fullName + "'");
			}
			
			themeList.put(fullName, theme);
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while trying to register theme", e);
		}
	}
	
	public static void RegisterTheme(ThemeBase theme, String idName)
	{
		try
		{
			ModContainer mod = Loader.instance().activeModContainer();
			
			if(idName.contains(":"))
			{
				throw new IllegalArgumentException("Illegal character(s) used in theme ID name");
			}
			
			if(theme == null)
			{
				throw new NullPointerException("Tried to register null theme");
			} else if(mod == null)
			{
				throw new IllegalArgumentException("Tried to register a theme without an active mod instance");
			}
			
			String fullName = mod.getModId() + ":" + idName;
			
			if(themeList.containsKey(fullName) || themeList.containsValue(theme))
			{
				throw new IllegalArgumentException("Cannot register dupliate theme '" + fullName + "'");
			}
			
			themeList.put(fullName, theme);
        	BetterQuesting.logger.log(Level.INFO, "Registered theme '" + theme.GetName() + "' (" + fullName + ")");
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while trying to register theme", e);
		}
	}
	
	/**
	 * Shortcut method for obtaining the currently selected theme
	 */
	public static ThemeBase curTheme()
	{
		return getTheme(BQ_Settings.curTheme);
	}
	
	public static ThemeBase getTheme(String id)
	{
		ThemeBase tmp = themeList.get(id);
		return tmp != null? tmp : fallbackTheme;
	}
	
	public static boolean themeExists(String id)
	{
		return themeList.get(id) != null;
	}
	
	public static void setTheme(String id)
	{
		BQ_Settings.curTheme = id;
		
		if(ConfigHandler.config != null)
		{
			ConfigHandler.config.get(Configuration.CATEGORY_GENERAL, "Theme", "").set(id);
			ConfigHandler.config.save();
		} else
		{
			BetterQuesting.logger.log(Level.WARN, "Unable to save theme setting");
		}
	}
	
	public static String getId(ThemeBase theme)
	{
		for(Entry<String,ThemeBase> entry : themeList.entrySet())
		{
			if(entry.getValue() == theme)
			{
				return entry.getKey();
			}
		}
		
		return "";
	}
	
	public static ArrayList<ThemeBase> GetAllThemes()
	{
		return new ArrayList<ThemeBase>(themeList.values());
	}
}
