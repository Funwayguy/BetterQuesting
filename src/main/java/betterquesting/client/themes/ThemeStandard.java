package betterquesting.client.themes;

import java.awt.Color;
import net.minecraft.util.ResourceLocation;

/**
 * Class for making standard themes with no special functionality. If you do require more functionality, create your own type originating from ThemeBase
 */
public class ThemeStandard extends ThemeBase
{
	final String name;
	final ResourceLocation guiTexture;
	Color txtColor = Color.BLACK;
	Color[] lineColors = new Color[]{new Color(0.75F, 0F, 0F), Color.YELLOW, Color.GREEN};
	Color[] iconColors = new Color[]{Color.GRAY, new Color(0.75F, 0F, 0F), new Color(0F, 1F, 1F), Color.GREEN};
	
	public ThemeStandard(String name, ResourceLocation texture)
	{
		this.name = name;
		this.guiTexture = texture;
	}
	
	@Override
	public String GetName()
	{
		return name;
	}
	
	@Override
	public ResourceLocation guiTexture()
	{
		return guiTexture;
	}
	
	public ThemeStandard setTextColor(Color c)
	{
		txtColor = c;
		return this;
	}
	
	public ThemeStandard setLineColors(Color locked, Color incomplete, Color complete)
	{
		lineColors[0] = locked;
		lineColors[1] = incomplete;
		lineColors[2] = complete;
		return this;
	}
	
	public ThemeStandard setIconColors(Color locked, Color incomplete, Color pending, Color complete)
	{
		iconColors[0] = locked;
		iconColors[1] = incomplete;
		iconColors[2] = pending;
		iconColors[3] = complete;
		return this;
	}
	
	@Override
	public Color textColor()
	{
		return txtColor;
	}
	
	@Override
	public Color lineColor(int questState, boolean isMain)
	{
		if(questState >= 0 && questState < lineColors.length)
		{
			return lineColors[questState];
		} else
		{
			return Color.GRAY;
		}
	}
	
	@Override
	public Color iconColor(int questState, boolean isMain)
	{
		if(questState >= 0 && questState < iconColors.length)
		{
			return iconColors[questState];
		} else
		{
			return Color.GRAY;
		}
	}
}
