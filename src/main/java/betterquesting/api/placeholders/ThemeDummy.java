package betterquesting.api.placeholders;

import java.awt.Color;
import betterquesting.api.client.themes.ITheme;
import betterquesting.api.client.themes.IThemeRenderer;
import net.minecraft.util.ResourceLocation;

/**
 * A dummy theme used by GuiElements when BetterQuesting isn't loaded
 */
public final class ThemeDummy implements ITheme
{
	public static final ThemeDummy INSTANCE = new ThemeDummy();
	
	private final ResourceLocation TEX = new ResourceLocation("missingno");
	private final ResourceLocation ID = new ResourceLocation("NULL");
	
	private ThemeDummy()
	{
	}
	
	@Override
	public ResourceLocation getThemeID()
	{
		return ID;
	}
	
	@Override
	public String getDisplayName()
	{
		return "NULL";
	}
	
	@Override
	public ResourceLocation getGuiTexture()
	{
		return TEX;
	}
	
	@Override
	public int getTextColor()
	{
		return Color.BLACK.getRGB();
	}

	@Override
	public IThemeRenderer getRenderer()
	{
		return new ThemeRenderDummy();
	}
}
