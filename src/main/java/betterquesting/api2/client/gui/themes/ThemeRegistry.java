package betterquesting.api2.client.gui.themes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.themes.ITheme;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.IGuiLine;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.resources.SimpleLine;
import betterquesting.api2.client.gui.resources.SlicedTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.core.BetterQuesting;

public class ThemeRegistry
{
	public static final ThemeRegistry INSTANCE = new ThemeRegistry();
	private static final IGuiTexture NULL_TEXTURE = new SlicedTexture(new ResourceLocation(BetterQuesting.MODID, "textures/gui/null_texture.png"), new GuiRectangle(0,0,32,32), new GuiPadding(8,8,8,8));
	private static final IGuiLine NULL_LINE = new SimpleLine();
	
	private final HashMap<ResourceLocation, IGuiTexture> defTextures = new HashMap<ResourceLocation, IGuiTexture>();
	private final HashMap<ResourceLocation, IGuiLine> defLines = new HashMap<ResourceLocation, IGuiLine>();
	private final HashMap<ResourceLocation, Integer> defColors = new HashMap<ResourceLocation, Integer>();
	
	private final HashMap<ResourceLocation, IGuiTheme> themes = new HashMap<ResourceLocation, IGuiTheme>();
	private IGuiTheme activeTheme = null;
	
	private ThemeRegistry()
	{
		PresetTexture.registerTextures(this);
		PresetIcon.registerIcons(this);
		PresetLine.registerLines(this);
		PresetColor.registerColors(this);
	}
	
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
	}
	
	/**
	 * Sets the default fallback texture. Only use if you're defining your own custom texture ID
	 */
	public void setDefaultTexture(ResourceLocation key, IGuiTexture tex)
	{
		if(key == null || tex == null)
		{
			throw new IllegalArgumentException("Tried to register a texture with one or more NULL arguments");
		} else if(defTextures.containsKey(key))
		{
			throw new IllegalArgumentException("Tried to overwrite existing default texture: " + key);
		} else
		{
			defTextures.put(key, tex);
		}
	}
	
	/**
	 * Sets the default fallback texture. Only use if you're defining your own custom texture ID
	 */
	public void setDefaultLine(ResourceLocation key, IGuiLine line)
	{
		if(key == null || line == null)
		{
			throw new IllegalArgumentException("Tried to register a line with one or more NULL arguments");
		} else if(defTextures.containsKey(key))
		{
			throw new IllegalArgumentException("Tried to overwrite existing default line: " + key);
		} else
		{
			defLines.put(key, line);
		}
	}
	
	/**
	 * Sets the default fallback texture. Only use if you're defining your own custom texture ID
	 */
	public void setDefaultColor(ResourceLocation key, int color)
	{
		if(defTextures.containsKey(key))
		{
			throw new IllegalArgumentException("Tried to overwrite existing default color: " + key);
		} else
		{
			defColors.put(key, color);
		}
	}
	
	public void setTheme(ResourceLocation id)
	{
		setTheme(themes.get(id));
	}
	
	public void setTheme(IGuiTheme theme)
	{
		this.activeTheme = theme;
	}
	
	public IGuiTheme getCurrentTheme()
	{
		return this.activeTheme;
	}
	
	public IGuiTexture getTexture(ResourceLocation key)
	{
		IGuiTexture tex = null;
		
		// TODO: Remove this when fully converted...
		ITheme legTheme = betterquesting.client.themes.ThemeRegistry.INSTANCE.getCurrentTheme();
		
		if(legTheme != null && (activeTheme == null || !activeTheme.getID().equals(legTheme.getThemeID())))
		{
			activeTheme = new LegacyThemeWrapper(legTheme);
		}
		
		if(activeTheme != null)
		{
			tex = activeTheme.getTexture(key);
		}
		
		tex = tex != null ? tex : defTextures.get(key);
		
		return tex == null? NULL_TEXTURE : tex;
	}
	
	public IGuiLine getLineRenderer(ResourceLocation key)
	{
		IGuiLine line = null;
		
		// TODO: Remove this when fully converted...
		ITheme legTheme = betterquesting.client.themes.ThemeRegistry.INSTANCE.getCurrentTheme();
		
		if(legTheme != null && (activeTheme == null || !activeTheme.getID().equals(legTheme.getThemeID())))
		{
			activeTheme = new LegacyThemeWrapper(legTheme);
		}
		
		if(activeTheme != null)
		{
			line = activeTheme.getLine(key);
		}
		
		line = line != null ? line : defLines.get(key);
		
		return line == null? NULL_LINE : line;
	}
	
	public int getColor(ResourceLocation key)
	{
		Integer color = null;
		
		// TODO: Remove this when fully converted...
		ITheme legTheme = betterquesting.client.themes.ThemeRegistry.INSTANCE.getCurrentTheme();
		
		if(legTheme != null && (activeTheme == null || !activeTheme.getID().equals(legTheme.getThemeID())))
		{
			activeTheme = new LegacyThemeWrapper(legTheme);
		}
		
		if(activeTheme != null)
		{
			color = activeTheme.getColor(key);
		}
		
		color = color < 0 ? color : defColors.get(key);
		
		return color == null? Color.BLACK.getRGB() : color;
	}
	
	public List<IGuiTheme> getAllThemes()
	{
		return new ArrayList<IGuiTheme>(themes.values());
	}
	
	public List<ResourceLocation> getAllThemeIDs()
	{
		return new ArrayList<ResourceLocation>(themes.keySet());
	}
}