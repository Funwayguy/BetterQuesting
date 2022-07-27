package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.util.ResourceLocation;

/**
 * Wraps an existing IGuiTexture with an IGuiColor
 */
public class GuiTextureColored implements IGuiTexture {
    private final IGuiTexture texture;
    private final IGuiColor color;

    public GuiTextureColored(IGuiTexture texture, IGuiColor color) {
        this.texture = texture;
        this.color = color;
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick) {
        if (width <= 0 || height <= 0) return;

        texture.drawTexture(x, y, width, height, zDepth, partialTick, color);
    }

    @Override
    @Deprecated
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor c) {
        if (width <= 0 || height <= 0) return;

        texture.drawTexture(x, y, width, height, zDepth, partialTick, c);
    }

    @Override
    public ResourceLocation getTexture() {
        return texture.getTexture();
    }

    @Override
    public IGuiRect getBounds() {
        return texture.getBounds();
    }
}
