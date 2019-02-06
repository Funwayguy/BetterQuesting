package betterquesting.api2.client.gui.themes;

import betterquesting.api.client.gui.misc.IGuiHook;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IThemeRegistry
{
    void registerTheme(IGuiTheme theme);
    
    IGuiTheme getCurrentTheme();
    IGuiTheme getTheme(ResourceLocation key);
    void setTheme(ResourceLocation key);
    
    void loadResourceThemes();
    
    IGuiTexture getTexture(ResourceLocation key);
    IGuiColor getColor(ResourceLocation key);
    IGuiLine getLine(ResourceLocation key);
    IGuiHook getGuiHook();
    
    void setDefaultTexture(ResourceLocation key, IGuiTexture tex);
    void setDefaultColor(ResourceLocation key, IGuiColor color);
    void setDefaultLine(ResourceLocation key, IGuiLine line);
    void setDefaultGuiHook(IGuiHook guiHook);
    
    List<IGuiTheme> getAllThemes();
}
