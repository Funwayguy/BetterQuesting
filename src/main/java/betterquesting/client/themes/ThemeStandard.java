package betterquesting.client.themes;

import java.awt.Color;
import net.minecraft.util.ResourceLocation;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonObject;

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
	
	public static ThemeStandard fromJson(JsonObject json)
	{
		if(json == null)
		{
			return null;
		}
		
		String name = JsonHelper.GetString(json, "name", "Unnamed Theme");
		String texture = JsonHelper.GetString(json, "texture", "betterquesting:textures/gui/editor_gui.png");
		
		int tColor = JsonHelper.GetNumber(json, "textColor", Color.BLACK.getRGB()).intValue();
		
		JsonObject jl = JsonHelper.GetObject(json, "lineColor");
		int lLocked = JsonHelper.GetNumber(jl, "locked", new Color(0.75F, 0F, 0F).getRGB()).intValue();
		int lPending = JsonHelper.GetNumber(jl, "pending", Color.YELLOW.getRGB()).intValue();
		int lComplete = JsonHelper.GetNumber(jl, "complete", Color.GREEN.getRGB()).intValue();
		
		JsonObject ji = JsonHelper.GetObject(json, "iconColor");
		int iLocked = JsonHelper.GetNumber(ji, "locked", Color.GRAY.getRGB()).intValue();
		int iPending = JsonHelper.GetNumber(ji, "pending", new Color(0.75F, 0F, 0F).getRGB()).intValue();
		int iUnclaimed = JsonHelper.GetNumber(ji, "unclaimed", new Color(0F, 1F, 1F).getRGB()).intValue();
		int iComplete = JsonHelper.GetNumber(ji, "complete", Color.GREEN.getRGB()).intValue();
		
		ThemeStandard theme = new ThemeStandard(name, new ResourceLocation(texture));
		theme.setTextColor(new Color(tColor));
		theme.setLineColors(new Color(lLocked), new Color(lPending), new Color(lComplete));
		theme.setIconColors(new Color(iLocked), new Color(iPending), new Color(iUnclaimed), new Color(iComplete));
		
		return theme;
	}
}
