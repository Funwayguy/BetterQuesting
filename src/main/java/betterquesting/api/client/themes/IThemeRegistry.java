package betterquesting.api.client.themes;

import java.util.List;
import net.minecraft.util.ResourceLocation;

public interface IThemeRegistry
{
	public void registerTheme(ITheme theme);
	public ITheme getTheme(ResourceLocation name);
	public List<ITheme> getAllThemes();
	
	public void registerLoader(IThemeLoader loader);
	public IThemeLoader getLoader(ResourceLocation name);
	public List<IThemeLoader> getAllLoaders();
	
	public void setCurrentTheme(ITheme theme);
	public ITheme getCurrentTheme();
	
	public void reloadThemes();
}
