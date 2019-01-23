package betterquesting.api2.client.gui.themes;

import betterquesting.api.client.themes.IThemeLoader;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.lines.SimpleLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.SlicedTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.client.gui2.GuiHome;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.ConfigHandler;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class ThemeRegistry implements IThemeRegistry
{
	public static final ThemeRegistry INSTANCE = new ThemeRegistry();
	
	private static final IGuiTexture NULL_TEXTURE = new SlicedTexture(PresetTexture.TX_NULL, new GuiRectangle(0,0,32,32), new GuiPadding(8,8,8,8));
	private static final IGuiLine NULL_LINE = new SimpleLine();
	private static final IGuiColor NULL_COLOR = new GuiColorStatic(0xFF000000);
	
	private final HashMap<ResourceLocation, IGuiTexture> defTextures = new HashMap<>();
	private final HashMap<ResourceLocation, IGuiLine> defLines = new HashMap<>();
	private final HashMap<ResourceLocation, IGuiColor> defColors = new HashMap<>();
	private Function<GuiScreen, GuiScreen> defHome;
	
	private final HashMap<ResourceLocation, IGuiTheme> themes = new HashMap<>();
	private final HashMap<ResourceLocation, IThemeLoader> loaders = new HashMap<>();
	private final List<ResourceLocation> loadedThemes = new ArrayList<>();
	
	private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private IGuiTheme activeTheme = null;
	
	private boolean setup = false;
	
	public ThemeRegistry()
	{
		PresetTexture.registerTextures(this);
		PresetIcon.registerIcons(this);
		PresetLine.registerLines(this);
		PresetColor.registerColors(this);
		defHome = GuiHome::new;
	}
	
	@Override
	public void registerTheme(IGuiTheme theme)
	{
		if(theme == null || theme.getID() == null)
		{
			throw new NullPointerException("Cannot register null theme");
		} else if(themes.containsKey(theme.getID()))
		{
			throw new IllegalArgumentException("Cannot register duplicate theme: " + theme.getID());
		}
		
		themes.put(theme.getID(), theme);
		
		if(activeTheme == null) setup = false; // A theme was registered that could possibly resolve the currently configured theme
	}
	
	@Override
    public void registerLoader(IThemeLoader loader)
    {
        if(loader == null || loader.getID() == null)
        {
            throw new NullPointerException("Cannot register null loader");
        } else if(loaders.containsKey(loader.getID()))
        {
            throw new IllegalArgumentException("Cannot register duplicate loader: " + loader.getID());
        }
        
        loaders.put(loader.getID(), loader);
    }
	
	/**
	 * Sets the default fallback texture. Only use if you're defining your own custom texture ID
	 */
	@Override
	public void setDefaultTexture(ResourceLocation key, IGuiTexture tex)
	{
		if(key == null || tex == null)
		{
			throw new NullPointerException("Tried to register a default theme texture with one or more NULL arguments");
		}
		
		defTextures.put(key, tex);
	}
	
	/**
	 * Sets the default fallback texture. Only use if you're defining your own custom texture ID
	 */
	@Override
	public void setDefaultLine(ResourceLocation key, IGuiLine line)
	{
		if(key == null || line == null)
		{
			throw new NullPointerException("Tried to register a default theme line with one or more NULL arguments");
		}
		
        defLines.put(key, line);
	}
	
	/**
	 * Sets the default fallback texture. Only use if you're defining your own custom texture ID
	 */
	@Override
	public void setDefaultColor(ResourceLocation key, IGuiColor color)
	{
	    if(key == null || color == null)
        {
            throw new NullPointerException("Tried to register default theme colour with one or more NULL arguments");
        }
		
        defColors.put(key, color);
	}
	
	@Override
	public void setDefaultHome(Function<GuiScreen, GuiScreen> ctor)
    {
        if(ctor == null)
        {
            throw new NullPointerException("Tried to register a default home screen with one or more NULL arguments");
        }
        
        defHome = ctor;
    }
	
	@Override
	public void setTheme(ResourceLocation id)
	{
		setTheme(themes.get(id));
	}
	
	@Override
    public IGuiTheme getTheme(ResourceLocation key)
    {
        return themes.get(key);
    }
	
	public void setTheme(IGuiTheme theme)
	{
		this.activeTheme = theme;
		
		BQ_Settings.curTheme = theme.getID().toString();
		
		if(ConfigHandler.config != null)
		{
			ConfigHandler.config.get(Configuration.CATEGORY_GENERAL, "Theme", "").set(BQ_Settings.curTheme);
			ConfigHandler.config.save();
			BetterQuesting.logger.log(Level.INFO, "Theme set to " + theme.getName());
		} else
		{
			BetterQuesting.logger.log(Level.WARN, "Unable to save theme setting");
		}
	}
	
	@Override
	public IGuiTheme getCurrentTheme()
	{
	    if(!setup && this.activeTheme == null)
        {
            this.activeTheme = this.getTheme(new ResourceLocation(BQ_Settings.curTheme));
            setup = true;
        }
        
		return this.activeTheme;
	}
	
	@Override
    public IThemeLoader getLoader(ResourceLocation key)
    {
        return loaders.get(key);
    }
    
    @Override
    public void reloadThemes()
    {
        loadedThemes.forEach(themes::remove);
        loadedThemes.clear();
        
        IResourceManager resManager = Minecraft.getMinecraft().getResourceManager();
        
        for(String domain : resManager.getResourceDomains())
        {
            ResourceLocation res = new ResourceLocation(domain, "bq_themes.json");
            List<IResource> list;
            
            try
            {
                list = resManager.getAllResources(res);
            } catch (Exception e)
            {
                continue;
            }
            
            for(IResource iresource : list)
            {
                try
                {
                    InputStreamReader isr = new InputStreamReader(iresource.getInputStream());
                    JsonArray jAry = GSON.fromJson(isr, JsonArray.class);
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
                            BetterQuesting.logger.error("Unable to find loader \"" + loadID + "\" for JSON theme in " + domain);
                            continue;
                        }
                        
                        IGuiTheme theme = loader.loadTheme(jThm, domain);
                        if(theme == null)
                        {
                            BetterQuesting.logger.error("Failed to load theme in " + domain);
                            continue;
                        } else if(themes.containsKey(theme.getID()))
                        {
                            BetterQuesting.logger.error("Unable to register JSON theme with duplicate ID: " + theme.getID());
                        }
                        
                        themes.put(theme.getID(), theme);
                        loadedThemes.add(theme.getID());
                    }
                } catch (Exception e)
                {
                    BetterQuesting.logger.error("Error reading bq_themes.json from " + domain, e);
                }
            }
        }
    }
	
	@Override
	public IGuiTexture getTexture(ResourceLocation key)
	{
		if(key == null) return NULL_TEXTURE;
		
		IGuiTexture tex = null;
		
		if(getCurrentTheme() != null) tex = activeTheme.getTexture(key);
		if(tex == null) tex = defTextures.get(key);
		return tex == null? NULL_TEXTURE : tex;
	}
	
	@Override
	public IGuiLine getLine(ResourceLocation key)
	{
		if(key == null) return NULL_LINE;
		
		IGuiLine line = null;
		
		if(getCurrentTheme() != null) line = activeTheme.getLine(key);
		if(line == null) line = defLines.get(key);
		return line == null? NULL_LINE : line;
	}
	
	@Override
	public IGuiColor getColor(ResourceLocation key)
	{
		if(key == null) return NULL_COLOR;
		
		IGuiColor color = null;
		
		if(getCurrentTheme() != null) color = activeTheme.getColor(key);
		if(color == null) color = defColors.get(key);
		return color == null? NULL_COLOR : color;
	}
	
	@Override
	public GuiScreen getHomeGui(GuiScreen parent)
    {
        GuiScreen screen = null;
        
        if(getCurrentTheme() != null) screen = activeTheme.getHomeGui(parent);
        if(screen == null && defHome != null) screen = defHome.apply(parent);
        
        return screen;
    }
	
	@Override
	public List<IGuiTheme> getAllThemes()
	{
		return new ArrayList<>(themes.values());
	}
	
	@Override
    public List<IThemeLoader> getAllLoaders()
    {
        return new ArrayList<>(loaders.values());
    }
}