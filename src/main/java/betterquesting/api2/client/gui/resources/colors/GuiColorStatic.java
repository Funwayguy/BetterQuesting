package betterquesting.api2.client.gui.resources.colors;

import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class GuiColorStatic implements IGuiColor {
    private final int argb;

    public GuiColorStatic(int color) {
        this.argb = color;
    }

    public GuiColorStatic(int red, int green, int blue, int alpha) {
        this.argb = ((alpha & 255) << 24) | ((red & 255) << 16) | ((green & 255) << 8) | (blue & 255);
    }

    public GuiColorStatic(float red, float green, float blue, float alpha) {
        this((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
    }

    public GuiColorStatic(Color color) {
        this(color.getRGB());
    }

    @Override
    public int getRGB() {
        return argb;
    }

    @Override
    public float getRed() {
        return (argb >> 16 & 255) / 255F;
    }

    @Override
    public float getGreen() {
        return (argb >> 8 & 255) / 255F;
    }

    @Override
    public float getBlue() {
        return (argb & 255) / 255F;
    }

    @Override
    public float getAlpha() {
        return (argb >> 24 & 255) / 255F;
    }

    @Override
    public void applyGlColor() {
        GlStateManager.color(getRed(), getGreen(), getBlue(), getAlpha());
    }
}
