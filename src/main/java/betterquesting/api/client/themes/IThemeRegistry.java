package betterquesting.api.client.themes;

import net.minecraft.util.ResourceLocation;

import java.util.List;

@Deprecated
public interface IThemeRegistry
{
	void registerTheme(ITheme theme);
	ITheme getTheme(ResourceLocation name);
	List<ITheme> getAllThemes();
	
	void registerLoader(IThemeLoader loader);
	IThemeLoader getLoader(ResourceLocation name);
	List<IThemeLoader> getAllLoaders();
	
	void setCurrentTheme(ITheme theme);
	ITheme getCurrentTheme();
	
	void reloadThemes();
}
