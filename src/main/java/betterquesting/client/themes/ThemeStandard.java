package betterquesting.client.themes;

import java.awt.Color;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.themes.ITheme;
import betterquesting.api.client.themes.IThemeRenderer;

public class ThemeStandard implements ITheme
{
	private final ThemeRenderStandard renderer = new ThemeRenderStandard();
	private final ResourceLocation regName;
	private final String name;
	private final ResourceLocation guiTexture;
	private int txtColor = Color.BLACK.getRGB();
	
	public ThemeStandard(String name, ResourceLocation texture, ResourceLocation regName)
	{
		this.regName = regName;
		this.name = name;
		this.guiTexture = texture;
	}
	
	@Override
	public ResourceLocation getThemeID()
	{
		return regName;
	}
	
	@Override
	public String getDisplayName()
	{
		return name;
	}
	
	@Override
	public ResourceLocation getGuiTexture()
	{
		return guiTexture;
	}
	
	public ThemeStandard setTextColor(int c)
	{
		txtColor = c;
		return this;
	}
	
	public ThemeStandard setLineColors(int locked, int incomplete, int complete)
	{
		renderer.setLineColors(locked, incomplete, complete);
		return this;
	}
	
	public ThemeStandard setIconColors(int locked, int incomplete, int pending, int complete)
	{
		renderer.setIconColors(locked, incomplete, pending, complete);
		return this;
	}
	
	@Override
	public int getTextColor()
	{
		return txtColor;
	}

	@Override
	public IThemeRenderer getRenderer()
	{
		return renderer;
	}
}
