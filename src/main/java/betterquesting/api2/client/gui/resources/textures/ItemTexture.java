package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

// Wrapper to allow embedding items into panels as IGuiTextures
public class ItemTexture implements IGuiTexture {
    private static final IGuiColor defColor = new GuiColorStatic(255, 255, 255, 255);

    private final BigItemStack stack;
    private final boolean showCount;
    private final boolean keepAspect;

    private float zDepth = 16F;

    // Dummy value
    private final IGuiRect bounds = new GuiRectangle(0, 0, 16, 16);

    public ItemTexture(BigItemStack stack) {
        this(stack, false, true);
    }

    public ItemTexture(BigItemStack stack, boolean showCount, boolean keepAspect) {
        this.stack = stack;
        this.showCount = showCount;
        this.keepAspect = keepAspect;
    }

    public ItemTexture setDepth(float z) {
        this.zDepth = z;
        return this;
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zLevel, float partialTick) {
        drawTexture(x, y, width, height, zLevel, partialTick, defColor);
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zLevel, float partialTick, IGuiColor color) {
        if (width <= 0 || height <= 0) return;

        float sx = width / 16F;
        float sy = height / 16F;

        double dx = 0;
        double dy = 0;

        if (keepAspect) {
            float sa = Math.min(sx, sy);

            dx = Math.floor((sx - sa) * 8F);
            dy = Math.floor((sy - sa) * 8F);

            sx = sa;
            sy = sa;
        }

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + dx, y + dy, 0);
        GlStateManager.scale(sx, sy, 1F);
        color.applyGlColor();

        RenderUtils.RenderItemStack(Minecraft.getMinecraft(), stack.getBaseStack(), 0, 0, zDepth, (showCount && stack.stackSize > 1) ? ("" + stack.stackSize) : "", 0xFFFFFFFF);

        GlStateManager.popMatrix();
    }

    @Override
    public ResourceLocation getTexture() {
        return PresetTexture.TX_NULL;
    }

    @Override
    public IGuiRect getBounds() {
        return bounds;
    }
}
