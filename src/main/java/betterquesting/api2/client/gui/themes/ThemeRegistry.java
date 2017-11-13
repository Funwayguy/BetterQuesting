package betterquesting.api2.client.gui.themes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.resources.SlicedTexture;
import betterquesting.core.BetterQuesting;

public class ThemeRegistry
{
	public static final ThemeRegistry INSTANCE = new ThemeRegistry();
	private static final IGuiTexture NULL_TEXTURE = new SlicedTexture(new ResourceLocation(BetterQuesting.MODID, "textures/gui/null_texture.png"), new GuiRectangle(0,0,32,32), new GuiPadding(8,8,8,8));
	
	private final HashMap<ResourceLocation, IGuiTexture> defTextures = new HashMap<ResourceLocation, IGuiTexture>();
	private final HashMap<ResourceLocation, IGuiTheme> themes = new HashMap<ResourceLocation, IGuiTheme>();
	private IGuiTheme activeTheme = null;
	
	private ThemeRegistry()
	{
		TexturePreset.initPresets(this);
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
		if(defTextures.containsKey(key))
		{
			throw new IllegalArgumentException("Tried to overwrite existing default texture: " + key);
		} else
		{
			defTextures.put(key, tex);
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
	
	public IGuiTexture getTexture(ResourceLocation key)
	{
		IGuiTexture tex = null;
		
		if(activeTheme != null)
		{
			tex = activeTheme.getTexture(key);
		}
		
		tex = tex != null ? tex : defTextures.get(key);
		
		return tex == null? NULL_TEXTURE : tex;
	}
	
	public List<ResourceLocation> getAllThemes()
	{
		return new ArrayList<ResourceLocation>(themes.keySet());
	}
}