package betterquesting.api2.client.gui.themes;

import betterquesting.api.client.themes.IThemeLoader;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Function;

public interface IThemeRegistry
{
    void registerTheme(IGuiTheme theme);
    void registerLoader(IThemeLoader loader);
    
    IGuiTheme getCurrentTheme();
    IGuiTheme getTheme(ResourceLocation key);
    void setTheme(ResourceLocation key);
    
    IThemeLoader getLoader(ResourceLocation key);
    void reloadThemes();
    
    IGuiTexture getTexture(ResourceLocation key);
    IGuiColor getColor(ResourceLocation key);
    IGuiLine getLine(ResourceLocation key);
    GuiScreen getHomeGui(GuiScreen parent);
    
    void setDefaultTexture(ResourceLocation key, IGuiTexture tex);
    void setDefaultColor(ResourceLocation key, IGuiColor color);
    void setDefaultLine(ResourceLocation key, IGuiLine line);
    void setDefaultHome(Function<GuiScreen, GuiScreen> ctor);
    
    List<IGuiTheme> getAllThemes();
    List<IThemeLoader> getAllLoaders();
}
