package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class PolyTexture implements IGuiTexture {
    private final IGuiColor defColor;
    private final boolean shadow;
    private final double[] verts;

    private int borderSize = 1;
    private IGuiColor borColor = new GuiColorStatic(0xFFFFFFFF);

    public PolyTexture(int points, double rotation, boolean shadow, @Nonnull IGuiColor color) {
        this.defColor = color;
        this.shadow = shadow;

        if (points <= 0) points = 32;
        verts = new double[points * 2]; // XY positions of all outer verticies
        double min = 0.0001D;

        // Generate points
        for (int i = 0; i < points; i++) {
            double angle = 360D * (i / (double) points);
            double x = -Math.sin(Math.toRadians(angle + rotation));
            double y = -Math.cos(Math.toRadians(angle + rotation));
            min = Math.max(min, Math.max(Math.abs(x), Math.abs(y)));

            verts[i * 2] = x;
            verts[i * 2 + 1] = y;
        }

        // Fit to 0D - 1D bounds
        for (int i = 0; i < verts.length; i++) {
            double x = verts[i];
            x = (x / 2D / min) + 0.5D;
            verts[i] = x;
        }
    }

    public PolyTexture(@Nonnull double[] verts, boolean shadow, IGuiColor color) {
        this.defColor = color;
        this.shadow = shadow;
        this.verts = new double[verts.length];
        System.arraycopy(verts, 0, this.verts, 0, verts.length);
    }

    public IGuiTexture setBorder(int size, IGuiColor color) {
        this.borderSize = size;
        this.borColor = color;
        return this;
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick) {
        drawTexture(x, y, width, height, zDepth, partialTick, defColor);
    }

    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color) {
        if (width <= 0 || height <= 0) return;

        GlStateManager.pushMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        int w = shadow ? width - 2 : width;
        int h = shadow ? height - 2 : height;
        int dx = shadow ? x + 1 : x;
        int dy = shadow ? y + 1 : y;
        int sx = x + 2;
        int sy = y + 2;

        int points = verts.length / 2;

        if (shadow) {
            GlStateManager.color(0F, 0F, 0F, 0.5F);
            vertexbuffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
            ;

            for (int i = 0; i < points + 1; i++) // Wraps around by one point
            {
                int index = (i % points) * 2;
                vertexbuffer.pos(sx + (w * verts[index]), sy + (h * verts[index + 1]), 0D).endVertex();
            }

            tessellator.draw();
        }

        color.applyGlColor();
        vertexbuffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        ;

        for (int i = 0; i < points + 1; i++) // Wraps around by one point
        {
            int index = (i % points) * 2;
            vertexbuffer.pos(dx + (w * verts[index]), dy + (h * verts[index + 1]), 0D).endVertex();
        }

        tessellator.draw();

        if (borderSize > 0 && borColor != null) {
            borColor.applyGlColor();
            vertexbuffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);

            for (int i = 0; i < points + 1; i++) // Wraps around by one point
            {
                int index = (i % points) * 2;
                double bx = (verts[index] - 0.5D) * -borderSize * 2D;
                double by = (verts[index + 1] - 0.5D) * -borderSize * 2D;
                vertexbuffer.pos(dx + (w * verts[index]) + bx, dy + (h * verts[index + 1]) + by, 0D).endVertex();
                vertexbuffer.pos(dx + (w * verts[index]), dy + (h * verts[index + 1]), 0D).endVertex();
            }

            tessellator.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
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
