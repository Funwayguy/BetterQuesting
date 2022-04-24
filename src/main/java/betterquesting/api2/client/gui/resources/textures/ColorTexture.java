package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ColorTexture implements IGuiTexture {
    private final IGuiColor baseColor;
    private final GuiRectangle rect = new GuiRectangle(0, 0, 0, 0);
    private final GuiPadding pad;
    // Dummy value
    private final IGuiRect bounds = new GuiRectangle(0, 0, 16, 16);

    public ColorTexture(IGuiColor baseColor) {
        this(baseColor, new GuiPadding(0, 0, 0, 0));
    }

    // Really only used for item slot highlighting but could be used elsewhere
    public ColorTexture(IGuiColor baseColor, GuiPadding padding) {
        this.baseColor = baseColor;
        this.pad = padding;
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick) {
        drawTexture(x, y, width, height, zDepth, partialTick, baseColor);
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color) {
        if (width <= 0 || height <= 0) return;

        GlStateManager.pushMatrix();

        // Just so we're not wasting heap memory making a new GuiRectangle every time
        rect.x = x + pad.l;
        rect.y = y + pad.t;
        rect.w = width - (pad.r + pad.l);
        rect.h = height - (pad.b + pad.r);

        RenderUtils.drawColoredRect(rect, color);

        GlStateManager.popMatrix();
    }

    @Override
    public ResourceLocation getTexture() {
        return PresetTexture.TX_SIMPLE;
    }

    @Override
    public IGuiRect getBounds() {
        return bounds;
    }
}
