package betterquesting.client.themes;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.themes.IGuiTheme;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class LegacyThemeLoader
{
    public static final LegacyThemeLoader INSTANCE = new LegacyThemeLoader();
    
	protected IGuiTheme loadTheme(JsonObject json, String domain)
	{
		if(json == null) return null;
		
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
		
    	String id = name.toLowerCase().trim().replaceAll(" ", "_");
    	ResourceLocation regName = new ResourceLocation(domain + ":" + id);
    	
		if(ThemeRegistry.INSTANCE.getTheme(regName) != null)
    	{
    		int i = 2;
        	regName = new ResourceLocation(domain + ":" + id + "_" + i);
    		
        	while(ThemeRegistry.INSTANCE.getTheme(regName) != null)
        	{
        		i++;
            	regName = new ResourceLocation(domain + ":" + id + "_" + i);
        	}
    	}
		
		ThemeLegacy theme = new ThemeLegacy(name, new ResourceLocation(texture), regName);
		theme.setTextColor(tColor);
		theme.setLineColors(lLocked, lPending, lComplete);
		theme.setIconColors(iLocked, iPending, iUnclaimed, iComplete);
		
		return theme;
	}
}
