package betterquesting.api.registry;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.themes.IThemeBase;
import betterquesting.api.client.themes.IThemeLoader;

public interface IThemeRegistry
{
	public void registerTheme(IThemeBase theme);
	public IThemeBase getTheme(ResourceLocation name);
	public List<IThemeBase> getAllThemes();
	
	public void registerLoader(IThemeLoader loader);
	public IThemeLoader getLoader(ResourceLocation name);
	public List<IThemeLoader> getAllLoaders();
	
	public void setCurrentTheme(IThemeBase theme);
	public IThemeBase getCurrentTheme();
	
	public void reloadThemes();
}
