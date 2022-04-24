package betterquesting.api2.client.gui.themes;

import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Function;

public interface IThemeRegistry {
    void registerTheme(IGuiTheme theme);

    IGuiTheme getCurrentTheme();

    IGuiTheme getTheme(ResourceLocation key);

    void setTheme(ResourceLocation key);

    void loadResourceThemes();

    IGuiTexture getTexture(ResourceLocation key);

    IGuiColor getColor(ResourceLocation key);

    IGuiLine getLine(ResourceLocation key);

    <T> GuiScreen getGui(GuiKey<T> key, T args);

    void setDefaultTexture(ResourceLocation key, IGuiTexture tex);

    void setDefaultColor(ResourceLocation key, IGuiColor color);

    void setDefaultLine(ResourceLocation key, IGuiLine line);

    <T> void setDefaultGui(GuiKey<T> key, Function<T, GuiScreen> func);

    List<IGuiTheme> getAllThemes();

    // === Future Editor Stuff ===
    ResourceLocation[] getKnownTextures();

    ResourceLocation[] getKnownColors();

    ResourceLocation[] getKnownLines();

    GuiKey[] getKnownGuis();
}
