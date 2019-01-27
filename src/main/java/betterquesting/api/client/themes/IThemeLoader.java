package betterquesting.api.client.themes;

import betterquesting.api2.client.gui.themes.IGuiTheme;
import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonObject;

@Deprecated
public interface IThemeLoader
{
	ResourceLocation getID();
	IGuiTheme loadTheme(JsonObject json, String domain);
}