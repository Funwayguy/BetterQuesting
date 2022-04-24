package betterquesting.api2.client.gui.resources.lines;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class SimpleLine implements IGuiLine {
    private final short pattern;
    private final int scale;

    public SimpleLine() {
        this(1, (short) 0xFFFF);
    }

    public SimpleLine(int stippleScale, short stipplePattern) {
        this.pattern = stipplePattern;
        this.scale = stippleScale;
    }

    @Override
    public void drawLine(IGuiRect start, IGuiRect end, int width, IGuiColor color, float partialTick) {
        GlStateManager.pushMatrix();

        GlStateManager.disableTexture2D();
        GL11.glEnable(GL11.GL_LINE_STIPPLE);
        color.applyGlColor();
        GL11.glLineWidth(width);
        GL11.glLineStipple(scale, pattern);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(start.getX() + start.getWidth() / 2F, start.getY() + start.getHeight() / 2F);
        GL11.glVertex2f(end.getX() + end.getWidth() / 2F, end.getY() + end.getHeight() / 2F);
        GL11.glEnd();

        GL11.glLineStipple(1, (short) 0xFFFF);
        GL11.glLineWidth(1F);
        GL11.glDisable(GL11.GL_LINE_STIPPLE);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1F, 1F, 1F, 1F);

        GlStateManager.popMatrix();
    }
}
