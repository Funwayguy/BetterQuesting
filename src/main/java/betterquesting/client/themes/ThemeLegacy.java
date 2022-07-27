package betterquesting.client.themes;

import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.colors.GuiColorPulse;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.SlicedTexture;
import betterquesting.api2.client.gui.resources.textures.SlicedTexture.SliceMode;
import betterquesting.api2.client.gui.themes.GuiKey;
import betterquesting.api2.client.gui.themes.IGuiTheme;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import java.util.HashMap;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

// Doesn't support anything fancy and is merely here to cut up the old layout into something usable in BQ3
public class ThemeLegacy implements IGuiTheme {
    private final String DISP_NAME;
    private final ResourceLocation ID_NAME;

    private final HashMap<ResourceLocation, IGuiTexture> TEX_MAP = new HashMap<>();
    private final HashMap<ResourceLocation, IGuiColor> COLOR_MAP = new HashMap<>();
    // private final HashMap<ResourceLocation, IGuiLine> LINE_MAP = new HashMap<>();

    public ThemeLegacy(String name, ResourceLocation texture, ResourceLocation regName) {
        this.DISP_NAME = name;
        this.ID_NAME = regName;

        TEX_MAP.put(
                PresetTexture.PANEL_MAIN.getKey(),
                new SlicedTexture(texture, new GuiRectangle(0, 0, 48, 48), new GuiPadding(16, 16, 16, 16))
                        .setSliceMode(SliceMode.SLICED_TILE));

        TEX_MAP.put(
                PresetTexture.BTN_NORMAL_0.getKey(),
                new SlicedTexture(texture, new GuiRectangle(48, 0, 200, 20), new GuiPadding(2, 2, 2, 3))
                        .setSliceMode(SliceMode.SLICED_TILE));
        TEX_MAP.put(
                PresetTexture.BTN_NORMAL_1.getKey(),
                new SlicedTexture(texture, new GuiRectangle(48, 20, 200, 20), new GuiPadding(2, 2, 2, 3))
                        .setSliceMode(SliceMode.SLICED_TILE));
        TEX_MAP.put(
                PresetTexture.BTN_NORMAL_2.getKey(),
                new SlicedTexture(texture, new GuiRectangle(48, 40, 200, 20), new GuiPadding(2, 2, 2, 3))
                        .setSliceMode(SliceMode.SLICED_TILE));

        TEX_MAP.put(
                PresetTexture.BTN_CLEAN_0.getKey(),
                new SlicedTexture(texture, new GuiRectangle(48, 0, 200, 20), new GuiPadding(2, 2, 2, 3))
                        .setSliceMode(SliceMode.SLICED_TILE));
        TEX_MAP.put(
                PresetTexture.BTN_CLEAN_1.getKey(),
                new SlicedTexture(texture, new GuiRectangle(48, 20, 200, 20), new GuiPadding(2, 2, 2, 3))
                        .setSliceMode(SliceMode.SLICED_TILE));
        TEX_MAP.put(
                PresetTexture.BTN_CLEAN_2.getKey(),
                new SlicedTexture(texture, new GuiRectangle(48, 40, 200, 20), new GuiPadding(2, 2, 2, 3))
                        .setSliceMode(SliceMode.SLICED_TILE));

        TEX_MAP.put(
                PresetTexture.SCROLL_V_BG.getKey(),
                new SlicedTexture(texture, new GuiRectangle(248, 0, 8, 60), new GuiPadding(1, 1, 1, 1))
                        .setSliceMode(SliceMode.SLICED_STRETCH));
        TEX_MAP.put(
                PresetTexture.SCROLL_V_0.getKey(),
                new SlicedTexture(texture, new GuiRectangle(248, 60, 8, 20), new GuiPadding(1, 1, 1, 1))
                        .setSliceMode(SliceMode.SLICED_STRETCH));
        TEX_MAP.put(
                PresetTexture.SCROLL_V_1.getKey(),
                new SlicedTexture(texture, new GuiRectangle(248, 60, 8, 20), new GuiPadding(1, 1, 1, 1))
                        .setSliceMode(SliceMode.SLICED_STRETCH));
        TEX_MAP.put(
                PresetTexture.SCROLL_V_2.getKey(),
                new SlicedTexture(texture, new GuiRectangle(248, 60, 8, 20), new GuiPadding(1, 1, 1, 1))
                        .setSliceMode(SliceMode.SLICED_STRETCH));

        TEX_MAP.put(
                PresetTexture.ITEM_FRAME.getKey(),
                new SlicedTexture(texture, new GuiRectangle(0, 48, 18, 18), new GuiPadding(1, 1, 1, 1))
                        .setSliceMode(SliceMode.SLICED_STRETCH));
        TEX_MAP.put(
                PresetTexture.AUX_FRAME_0.getKey(),
                new SlicedTexture(texture, new GuiRectangle(0, 128, 128, 128), new GuiPadding(1, 1, 1, 1))
                        .setSliceMode(SliceMode.SLICED_STRETCH));

        IGuiTexture qTexMain = new SlicedTexture(texture, new GuiRectangle(0, 104, 24, 24), new GuiPadding(8, 8, 8, 8));
        TEX_MAP.put(PresetTexture.QUEST_NORM_0.getKey(), qTexMain);
        TEX_MAP.put(PresetTexture.QUEST_NORM_1.getKey(), qTexMain);
        TEX_MAP.put(PresetTexture.QUEST_NORM_2.getKey(), qTexMain);
        TEX_MAP.put(PresetTexture.QUEST_NORM_3.getKey(), qTexMain);

        IGuiTexture qTexNorm =
                new SlicedTexture(texture, new GuiRectangle(24, 104, 24, 24), new GuiPadding(8, 8, 8, 8));
        TEX_MAP.put(PresetTexture.QUEST_MAIN_0.getKey(), qTexNorm);
        TEX_MAP.put(PresetTexture.QUEST_MAIN_1.getKey(), qTexNorm);
        TEX_MAP.put(PresetTexture.QUEST_MAIN_2.getKey(), qTexNorm);
        TEX_MAP.put(PresetTexture.QUEST_MAIN_3.getKey(), qTexNorm);
    }

    @Override
    public String getName() {
        return DISP_NAME;
    }

    @Override
    public ResourceLocation getID() {
        return ID_NAME;
    }

    @Override
    public IGuiTexture getTexture(ResourceLocation key) {
        return TEX_MAP.get(key);
    }

    @Override
    public IGuiLine getLine(ResourceLocation key) {
        return null;
    }

    @Override
    public IGuiColor getColor(ResourceLocation key) {
        return COLOR_MAP.get(key);
    }

    @Nullable
    @Override
    public <T> Function<T, GuiScreen> getGui(GuiKey<T> key) {
        return null;
    }

    public ThemeLegacy setTextColor(int c) {
        IGuiColor col = new GuiColorStatic(c);
        COLOR_MAP.put(PresetColor.TEXT_HEADER.getKey(), col);
        COLOR_MAP.put(PresetColor.TEXT_MAIN.getKey(), col);
        return this;
    }

    public ThemeLegacy setLineColors(int locked, int incomplete, int complete) {
        COLOR_MAP.put(PresetColor.QUEST_LINE_LOCKED.getKey(), new GuiColorPulse(locked, darkenColor(locked), 1F, 0F));
        COLOR_MAP.put(
                PresetColor.QUEST_LINE_UNLOCKED.getKey(), new GuiColorPulse(incomplete, darkenColor(locked), 1F, 0F));
        COLOR_MAP.put(
                PresetColor.QUEST_LINE_PENDING.getKey(), new GuiColorPulse(complete, darkenColor(locked), 1F, 0F));
        COLOR_MAP.put(
                PresetColor.QUEST_LINE_COMPLETE.getKey(), new GuiColorPulse(complete, darkenColor(locked), 1F, 0F));
        return this;
    }

    public ThemeLegacy setIconColors(int locked, int incomplete, int pending, int complete) {
        COLOR_MAP.put(PresetColor.QUEST_ICON_LOCKED.getKey(), new GuiColorPulse(locked, darkenColor(locked), 1F, 0F));
        COLOR_MAP.put(
                PresetColor.QUEST_ICON_UNLOCKED.getKey(), new GuiColorPulse(incomplete, darkenColor(locked), 1F, 0F));
        COLOR_MAP.put(PresetColor.QUEST_ICON_PENDING.getKey(), new GuiColorPulse(pending, darkenColor(locked), 1F, 0F));
        COLOR_MAP.put(
                PresetColor.QUEST_ICON_COMPLETE.getKey(), new GuiColorPulse(complete, darkenColor(locked), 1F, 0F));
        COLOR_MAP.put(
                PresetColor.QUEST_ICON_REPEATABLE.getKey(), new GuiColorPulse(complete, darkenColor(locked), 1F, 0F));
        return this;
    }

    private int darkenColor(int c) {
        int a = (24 >> c) & 255;
        int r = (16 >> c) & 255;
        int g = (8 >> c) & 255;
        int b = c & 255;

        r = (int) (r * 0.75F);
        g = (int) (g * 0.75F);
        b = (int) (b * 0.75F);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
