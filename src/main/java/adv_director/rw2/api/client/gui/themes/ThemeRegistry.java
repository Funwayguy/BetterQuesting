package adv_director.rw2.api.client.gui.themes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.lwjgl.util.Rectangle;
import net.minecraft.util.ResourceLocation;
import adv_director.core.AdvDirector;
import adv_director.rw2.api.client.gui.misc.GuiPadding;
import adv_director.rw2.api.client.gui.resources.IGuiTexture;
import adv_director.rw2.api.client.gui.resources.SlicedTexture;

public class ThemeRegistry
{
	public static final ThemeRegistry INSTANCE = new ThemeRegistry();
	private static final IGuiTexture NULL_TEXTURE = new SlicedTexture(new ResourceLocation(AdvDirector.MODID, "textures/gui/null_texture.png"), new Rectangle(0,0,32,32), new GuiPadding(8,8,8,8));
	
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
		IGuiTexture tex = NULL_TEXTURE;
		tex = defTextures.get(key);
		
		if(activeTheme != null)
		{
			tex = activeTheme.getTexture(key);
		}
		
		return tex;
	}
	
	public List<ResourceLocation> getAllThemes()
	{
		return new ArrayList<ResourceLocation>(themes.keySet());
	}
}