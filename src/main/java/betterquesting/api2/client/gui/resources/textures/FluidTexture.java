package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class FluidTexture implements IGuiTexture {
    private static final IGuiColor defColor = new GuiColorStatic(255, 255, 255, 255);

    private final FluidStack fluid;
    private final boolean showCount;
    private final boolean keepAspect;

    // Dummy value
    private final IGuiRect bounds = new GuiRectangle(0, 0, 16, 16);

    public FluidTexture(FluidStack fluid) {
        this(fluid, false, true);
    }

    public FluidTexture(FluidStack fluid, boolean showCount, boolean keepAspect) {
        this.fluid = fluid;
        this.showCount = showCount;
        this.keepAspect = keepAspect;
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick) {
        this.drawTexture(x, y, width, height, zDepth, partialTick, defColor);
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color) {
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

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        int fCol = fluid.getFluid().getColor(fluid);
        float a = (fCol >> 24 & 255) / 255F;
        float r = (fCol >> 16 & 255) / 255F;
        float g = (fCol >> 8 & 255) / 255F;
        float b = (fCol & 255) / 255F;
        a = a + color.getAlpha() / 2F;
        r = r + color.getRed() / 2F;
        g = g + color.getGreen() / 2F;
        b = b + color.getBlue() / 2F;
        GlStateManager.color(r, g, b, a);

        // TODO: Add tiling option

        Minecraft mc = Minecraft.getMinecraft();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        TextureAtlasSprite fluidTx = mc.getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill().toString());
        this.drawTexturedModalRect(0, 0, 0, fluidTx, 16, 16);

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

    private void drawTexturedModalRect(double xCoord, double yCoord, double zDepth, TextureAtlasSprite textureSprite, double widthIn, double heightIn) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(xCoord, yCoord + heightIn, zDepth).tex(textureSprite.getMinU(), textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos(xCoord + widthIn, yCoord + heightIn, zDepth).tex(textureSprite.getMaxU(), textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos(xCoord + widthIn, yCoord, zDepth).tex(textureSprite.getMaxU(), textureSprite.getMinV()).endVertex();
        bufferbuilder.pos(xCoord, yCoord, zDepth).tex(textureSprite.getMinU(), textureSprite.getMinV()).endVertex();
        tessellator.draw();
    }
}
