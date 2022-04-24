package betterquesting.api2.client.gui.resources.colors;

import betterquesting.api.utils.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;

public class GuiColorPulse implements IGuiColor {
    // Saves me having to run the math function every frame
    private final static double RAD = Math.toRadians(360F);

    private final IGuiColor c1;
    private final IGuiColor c2;

    private final double period;
    private final float phase;

    public GuiColorPulse(int color1, int color2, double period, float phase) {
        this(new GuiColorStatic(color1), new GuiColorStatic(color2), period, phase);
    }

    public GuiColorPulse(IGuiColor color1, IGuiColor color2, double period, float phase) {
        this.c1 = color1;
        this.c2 = color2;

        this.period = period;
        this.phase = phase;
    }

    @Override
    public int getRGB() {
        // Period in milliseconds
        double pms = 1000D * period;
        // Current period time
        double time = System.currentTimeMillis() % pms;
        // Shift current time by phase, wrap value and scale between 0.0 - 1.0
        time = (time + (pms * phase)) % pms / pms;
        // Convert time to sine wave between 0.0 and 1.0
        float blend = (float) (Math.cos(time * RAD) / 2D + 0.5D);
        // Return interpolated color
        return RenderUtils.lerpRGB(c1.getRGB(), c2.getRGB(), blend);
    }

    @Override
    public float getRed() {
        return (getRGB() >> 16 & 255) / 255F;
    }

    @Override
    public float getGreen() {
        return (getRGB() >> 8 & 255) / 255F;
    }

    @Override
    public float getBlue() {
        return (getRGB() & 255) / 255F;
    }

    @Override
    public float getAlpha() {
        return (getRGB() >> 24 & 255) / 255F;
    }

    @Override
    public void applyGlColor() {
        int color = getRGB();
        float a = (float) (color >> 24 & 255) / 255F;
        float r = (float) (color >> 16 & 255) / 255F;
        float g = (float) (color >> 8 & 255) / 255F;
        float b = (float) (color & 255) / 255F;
        GlStateManager.color(r, g, b, a);
    }
}
