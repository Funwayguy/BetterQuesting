package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class SlideShowTexture implements IGuiTexture {
    private final IGuiTexture[] slides;
    private final float interval;

    public SlideShowTexture(float interval, IGuiTexture... slides) {
        this.slides = slides;
        this.interval = interval;
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zLevel, float partialTick) {
        if (width <= 0 || height <= 0 || slides.length <= 0) return;

        IGuiTexture tex = getCurrentFrame();
        if (tex != null) tex.drawTexture(x, y, width, height, zLevel, partialTick);
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color) {
        if (width <= 0 || height <= 0 || slides.length <= 0) return;

        IGuiTexture tex = getCurrentFrame();
        if (tex != null) tex.drawTexture(x, y, width, height, zDepth, partialTick, color);
    }

    @Override
    @Deprecated
    public ResourceLocation getTexture() {
        IGuiTexture tex = getCurrentFrame();
        return tex == null ? null : tex.getTexture();
    }

    @Override
    @Deprecated
    public IGuiRect getBounds() {
        IGuiTexture tex = getCurrentFrame();
        return tex == null ? null : tex.getBounds();
    }

    @Nullable
    public IGuiTexture getCurrentFrame() {
        if (slides.length <= 0) return null;
        return slides[(int) Math.floor((System.currentTimeMillis() / 1000D) % (slides.length * interval) / interval)];
    }

    public IGuiTexture[] getAllFrames() {
        return slides;
    }
}
