package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.util.ResourceLocation;

// Literally does nothing...
public class EmptyTexture implements IGuiTexture {
    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick) {
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color) {
    }

    @Override
    public ResourceLocation getTexture() {
        return null;
    }

    @Override
    public IGuiRect getBounds() {
        return null;
    }
}
