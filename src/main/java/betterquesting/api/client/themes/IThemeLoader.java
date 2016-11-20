package betterquesting.api.client.themes;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonObject;

public interface IThemeLoader
{
	public ResourceLocation getLoaderID();
	public ITheme loadTheme(JsonObject json, String domain);
}