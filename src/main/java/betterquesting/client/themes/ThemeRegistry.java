package betterquesting.client.themes;

import betterquesting.api.client.gui.misc.IGuiHook;
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
import betterquesting.api2.client.gui.themes.IGuiTheme;
import betterquesting.api2.client.gui.themes.IThemeRegistry;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.registry.IFactoryJSON;
import betterquesting.client.GuiBuilder;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.ConfigHandler;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThemeRegistry implements IThemeRegistry
{
	public static final ThemeRegistry INSTANCE = new ThemeRegistry();
	
	private static final IGuiTexture NULL_TEXTURE = new SlicedTexture(PresetTexture.TX_NULL, new GuiRectangle(0,0,32,32), new GuiPadding(8,8,8,8));
	private static final IGuiLine NULL_LINE = new SimpleLine();
	private static final IGuiColor NULL_COLOR = new GuiColorStatic(0xFF000000);
	
	private final HashMap<ResourceLocation, IGuiTexture> defTextures = new HashMap<>();
	private final HashMap<ResourceLocation, IGuiLine> defLines = new HashMap<>();
	private final HashMap<ResourceLocation, IGuiColor> defColors = new HashMap<>();
	private IGuiHook defGuiHook;
	
	private final HashMap<ResourceLocation, IGuiTheme> themes = new HashMap<>();
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
		defGuiHook = GuiBuilder.INSTANCE;
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
	public void setDefaultGuiHook(IGuiHook guiHook)
    {
        if(guiHook == null)
        {
            throw new NullPointerException("Tried to register a default gui hook with one or more NULL arguments");
        }
        
        defGuiHook = guiHook;
    }
	
	@Override
	public void setTheme(ResourceLocation id)
	{
		setTheme(themes.get(id), id);
	}
	
	@Override
    public IGuiTheme getTheme(ResourceLocation key)
    {
        if(key == null) return null;
        return themes.get(key);
    }
	
	private void setTheme(IGuiTheme theme, ResourceLocation id)
	{
		this.activeTheme = theme;
		
		BQ_Settings.curTheme = id == null ? "" : id.toString();
		
		if(ConfigHandler.config != null)
		{
			ConfigHandler.config.get(Configuration.CATEGORY_GENERAL, "Theme", "").set(BQ_Settings.curTheme);
			ConfigHandler.config.save();
			BetterQuesting.logger.log(Level.INFO, "Theme set to " + BQ_Settings.curTheme);
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
    public void loadResourceThemes()
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
            } catch (Exception e) { continue; } // Not going to log errors everytime the file isn't found
            
            for(IResource iresource : list)
            {
                try
                {
                    InputStreamReader isr = new InputStreamReader(iresource.getInputStream());
                    JsonArray jAry = GSON.fromJson(isr, JsonArray.class);
                    isr.close();
                    
                    for(int i = 0; i < jAry.size(); i++)
                    {
                        JsonElement je = jAry.get(i);
                        
                        if(!(je instanceof JsonObject))
                        {
                            BetterQuesting.logger.log(Level.WARN, "Invalid theme entry at index " + i + " in " + iresource.getResourceLocation());
                            continue;
                        }
                        
                        JsonObject jThm = je.getAsJsonObject();
                        
                        if(jThm.has("themeType"))
                        {
                            BetterQuesting.logger.warn("Theme entry " + i + " in " + iresource.getResourceLocation());
                            loadLegacy(jThm, domain);
                            continue;
                        }
                        
                        ResourceLocation parentID = !jThm.has("themeParent") ? null : new ResourceLocation(JsonHelper.GetString(jThm, "themeParent", "minecraft:null"));
                        IGuiTheme parentTheme = getTheme(parentID);
                        String themeName = JsonHelper.GetString(jThm, "themeName", "Unnamed Theme");
                        String idRaw = themeName.toLowerCase().trim().replaceAll(" ", "_");
                        ResourceLocation themeId = new ResourceLocation(domain, idRaw);
                        
                        int n = 0;
                        while(themes.containsKey(themeId)) themeId = new ResourceLocation(domain, idRaw + n++);
                        
                        ResourceTheme resTheme;
                        
                        try
                        {
                            resTheme = new ResourceTheme(parentTheme, themeId, themeName);
                        } catch(Exception e)
                        {
                            BetterQuesting.logger.error("Failed to load theme entry " + i + " in " + iresource.getResourceLocation(), e);
                            continue;
                        }
                        
                        for(JsonElement jeTex : JsonHelper.GetArray(jThm, "textures"))
                        {
                            if(!(jeTex instanceof JsonObject)) continue;
                            JsonObject joTex = jeTex.getAsJsonObject();
                            
                            ResourceLocation typeID = new ResourceLocation(JsonHelper.GetString(joTex, "texType", ""));
                            IFactoryJSON<IGuiTexture, JsonObject> tFact = ResourceRegistry.INSTANCE.getTexReg().getFactory(typeID);
                            
                            if(tFact == null)
                            {
                                BetterQuesting.logger.error("Unknown texture type " + typeID + " for theme " + themeName + " in " + iresource.getResourceLocation());
                                continue;
                            }
                            
                            IGuiTexture gTex = tFact.loadFromJSON(joTex);
                            
                            if(gTex == null)
                            {
                                BetterQuesting.logger.error("Failed to load texture type " + typeID + " for theme " + themeName + " in " + iresource.getResourceLocation());
                                continue;
                            }
                            // TODO: Load textures
                        }
                        
                        for(JsonElement jeColor : JsonHelper.GetArray(jThm, "colors"))
                        {
                            if(!(jeColor instanceof JsonObject)) continue;
                            
                            // TODO: Load colors
                        }
                        
                        for(JsonElement jeLine : JsonHelper.GetArray(jThm, "lines"))
                        {
                            if(!(jeLine instanceof JsonObject)) continue;
                            
                            // TODO: Load lines
                        }
                        
                        themes.put(resTheme.getID(), resTheme);
                        loadedThemes.add(resTheme.getID());
                    }
                } catch (Exception e)
                {
                    BetterQuesting.logger.error("Error reading bq_themes.json from " + iresource.getResourceLocation(), e);
                }
            }
        }
    }
    
    private void loadLegacy(JsonObject json, String domain)
    {
        IGuiTheme theme = LegacyThemeLoader.INSTANCE.loadTheme(json, domain);
        
        if(theme == null)
        {
            BetterQuesting.logger.error("Failed to load legacy theme from " + domain);
            return;
        } else if(themes.containsKey(theme.getID()))
        {
            BetterQuesting.logger.error("Unable to register legacy resource theme with duplicate ID: " + theme.getID());
        }
        
        themes.put(theme.getID(), theme);
        loadedThemes.add(theme.getID());
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
	public IGuiHook getGuiHook()
    {
        IGuiHook screen = null;
        
        if(getCurrentTheme() != null) screen = activeTheme.getGuiHook();
        
        return screen != null ? screen : defGuiHook;
    }
	
	@Override
	public List<IGuiTheme> getAllThemes()
	{
		return new ArrayList<>(themes.values());
	}
}