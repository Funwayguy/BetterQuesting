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
	final Color txtColor;
	
	public ThemeStandard(String name, ResourceLocation texture, Color text)
	{
		this.name = name;
		this.guiTexture = texture;
		this.txtColor = text;
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
	
	@Override
	public Color textColor()
	{
		return txtColor;
	}
	
}
