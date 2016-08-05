package betterquesting.registry;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;
import betterquesting.api.client.themes.IThemeBase;
import betterquesting.api.client.themes.IThemeLoader;
import betterquesting.api.registry.IThemeRegistry;
import betterquesting.api.utils.JsonHelper;
import betterquesting.client.themes.ThemeLoaderStandard;
import betterquesting.client.themes.ThemeStandard;
import betterquesting.core.BQ_Settings;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.ConfigHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ThemeRegistry implements IThemeRegistry
{
	public static final ThemeRegistry INSTANCE = new ThemeRegistry();
	
	private final IThemeBase fallbackTheme = new ThemeStandard("Standard", new ResourceLocation(BetterQuesting.MODID, "textures/gui/editor_gui.png"), new ResourceLocation(BetterQuesting.MODID + ":fallback"));
	private IThemeBase currentTheme = null;
	
	private final HashMap<ResourceLocation,IThemeBase> themeList = new HashMap<ResourceLocation,IThemeBase>();
	private final HashMap<ResourceLocation,IThemeBase> resThemes = new HashMap<ResourceLocation,IThemeBase>();
	private final HashMap<ResourceLocation,IThemeLoader> themeLoaders = new HashMap<ResourceLocation,IThemeLoader>();
	
	private ThemeRegistry()
	{
		registerLoader(new ThemeLoaderStandard());
	}
	
	@Override
	public void registerTheme(IThemeBase theme)
	{
		if(theme == null)
		{
			throw new NullPointerException("Tried to register null theme");
		} else if(theme.getThemeID() == null)
		{
			throw new IllegalArgumentException("Tried to register a theme with a null name");
		}
		
		if(themeList.containsKey(theme.getThemeID()) || themeList.containsValue(theme))
		{
			throw new IllegalArgumentException("Cannot register dupliate theme '" + theme.getThemeID() + "'");
		}
		
		if(resThemes.containsKey(theme.getThemeID()) || resThemes.containsValue(theme))
		{
			throw new IllegalArgumentException("Cannot register dupliate theme '" + theme.getThemeID() + "'");
		}
		
		themeList.put(theme.getThemeID(), theme);
	}
	
	@Override
	public IThemeBase getTheme(ResourceLocation name)
	{
		IThemeBase tmp = themeList.get(name);
		tmp = tmp != null? tmp : resThemes.get(name);
		return tmp;
	}
	
	@Override
	public List<IThemeBase> getAllThemes()
	{
		ArrayList<IThemeBase> list = new ArrayList<IThemeBase>();
		list.addAll(themeList.values());
		list.addAll(resThemes.values());
		return list;
	}
	
	@Override
	public void registerLoader(IThemeLoader loader)
	{
		if(loader == null)
		{
			throw new NullPointerException("Tried to register null theme loader");
		} else if(loader.getLoaderID() == null)
		{
			throw new IllegalArgumentException("Tried to register a theme loader with a null name");
		}
		
		if(themeLoaders.containsKey(loader.getLoaderID()) || themeList.containsValue(loader))
		{
			throw new IllegalArgumentException("Cannot register dupliate theme loader '" + loader.getLoaderID() + "'");
		}
		
		themeLoaders.put(loader.getLoaderID(), loader);
	}
	
	@Override
	public IThemeLoader getLoader(ResourceLocation name)
	{
		return themeLoaders.get(name);
	}
	
	@Override
	public List<IThemeLoader> getAllLoaders()
	{
		return new ArrayList<IThemeLoader>(themeLoaders.values());
	}
	
	@Override
	public IThemeBase getCurrentTheme()
	{
		return currentTheme != null? currentTheme : fallbackTheme;
	}
	
	@Override
	public void setCurrentTheme(IThemeBase theme)
	{
		if(theme == null)
		{
			return;
		}
		
		currentTheme = theme;
		BQ_Settings.curTheme = theme.getThemeID().toString();
		
		if(ConfigHandler.config != null)
		{
			ConfigHandler.config.get(Configuration.CATEGORY_GENERAL, "Theme", "").set(BQ_Settings.curTheme);
			ConfigHandler.config.save();
		} else
		{
			BetterQuesting.logger.log(Level.WARN, "Unable to save theme setting");
		}
	}
	
	@Override
	public void reloadThemes()
	{
		resThemes.clear();
		
		IResourceManager resManager = Minecraft.getMinecraft().getResourceManager();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
        @SuppressWarnings("unchecked")
		Iterator<String> iterator = resManager.getResourceDomains().iterator();
        
        while(iterator.hasNext())
        {
            String domain = iterator.next();
            
            try
            {
            	ResourceLocation res = new ResourceLocation(domain, "bq_themes.json");
                @SuppressWarnings("unchecked")
				List<IResource> list = resManager.getAllResources(res);
                Iterator<IResource> iterator1 = list.iterator();
                
                while (iterator1.hasNext())
                {
                    IResource iresource = (IResource)iterator1.next();
                    
                    try
                    {
                    	InputStreamReader isr = new InputStreamReader(iresource.getInputStream());
                        JsonArray jAry = gson.fromJson(isr, JsonArray.class);
                        isr.close();
                        
                        for(JsonElement je : jAry)
                        {
                        	if(je == null || !je.isJsonObject())
                        	{
                        		BetterQuesting.logger.log(Level.WARN, "Invalid theme in " + domain);
                        		continue;
                        	}
                        	
                        	JsonObject jThm = je.getAsJsonObject();
                        	
                        	ResourceLocation loadID = new ResourceLocation(JsonHelper.GetString(jThm, "themeType", "betterquesting:standard"));
                        	IThemeLoader loader = getLoader(loadID);
                        	
                        	if(loader == null)
                        	{
                        		continue;
                        	}
                        	
                        	IThemeBase theme = loader.loadTheme(jThm, domain);
                        	
                        	if(theme != null)
                        	{
                        		resThemes.put(theme.getThemeID(), theme);
                        	}
                        }
                    } catch (Exception e)
                    {
                        BetterQuesting.logger.log(Level.ERROR, "Error reading bq_themes.json from " + domain, e);
                    }
                }
            } catch (Exception e){}
        }
	}
}
