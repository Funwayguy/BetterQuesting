package betterquesting.api.client.themes;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonObject;

@Deprecated
public interface IThemeLoader
{
	ResourceLocation getLoaderID();
	ITheme loadTheme(JsonObject json, String domain);
}