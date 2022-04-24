package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.util.ResourceLocation;

public class LayeredTexture implements IGuiTexture {
    private final IGuiTexture[] layers;

    public LayeredTexture(IGuiTexture... layers) {
        this.layers = layers;
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick) {
        if (width <= 0 || height <= 0) return;

        for (IGuiTexture tex : layers) {
            tex.drawTexture(x, y, width, height, zDepth, partialTick);
        }
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color) {
        if (width <= 0 || height <= 0) return;

        for (IGuiTexture tex : layers) {
            tex.drawTexture(x, y, width, height, zDepth, partialTick, color);
        }
    }

    @Override
    public ResourceLocation getTexture() {
        return layers.length <= 0 ? null : layers[0].getTexture();
    }

    @Override
    public IGuiRect getBounds() {
        return layers.length <= 0 ? null : layers[0].getBounds();
    }
}
