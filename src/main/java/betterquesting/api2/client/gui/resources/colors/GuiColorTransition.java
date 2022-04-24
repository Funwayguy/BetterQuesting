package betterquesting.api2.client.gui.resources.colors;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.IValueIO;
import net.minecraft.client.renderer.GlStateManager;

public class GuiColorTransition implements IGuiColor {
    private final IGuiColor cNorm;
    private final IGuiColor cLow;

    private boolean useLerp = true;
    private float threshold = 1F;
    private IValueIO<Float> driver = null;

    public GuiColorTransition(IGuiColor cNorm, IGuiColor cLow) {
        this.cNorm = cNorm;
        this.cLow = cLow;
    }

    public GuiColorTransition setupBlending(boolean enable, float threshold) {
        this.useLerp = enable;
        this.threshold = threshold;
        return this;
    }

    public GuiColorTransition setBlendDriver(IValueIO<Float> driver) {
        this.driver = driver;
        return this;
    }

    @Override
    public int getRGB() {
        if (driver != null && driver.readValue() < threshold) {
            if (!useLerp) {
                return cLow.getRGB();
            } else {
                return RenderUtils.lerpRGB(cLow.getRGB(), cNorm.getRGB(), driver.readValue() / threshold);
            }
        } else {
            return cNorm.getRGB();
        }
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
        int argb = getRGB();
        float r = (argb >> 16 & 255) / 255F;
        float g = (argb >> 8 & 255) / 255F;
        float b = (argb & 255) / 255F;
        float a = (argb >> 24 & 255) / 255F;

        GlStateManager.color(r, g, b, a);
    }
}
