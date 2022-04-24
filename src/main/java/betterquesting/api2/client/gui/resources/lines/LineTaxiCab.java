package betterquesting.api2.client.gui.resources.lines;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class LineTaxiCab implements IGuiLine {
    private final float bias;
    private final boolean isVertical;
    private final short stipMask;
    private final int stipScale;

    public LineTaxiCab() {
        this(0.5F, false, 1, (short) 0xFFFF);
    }

    public LineTaxiCab(float bias, boolean isVertical, int stipScale, short stipMask) {
        this.bias = MathHelper.clamp(bias, 0F, 1F);
        this.isVertical = isVertical;

        this.stipScale = stipScale;
        this.stipMask = stipMask;
    }

    @Override
    public void drawLine(IGuiRect start, IGuiRect end, int width, IGuiColor color, float partialTick) {
        GlStateManager.pushMatrix();

        GlStateManager.disableTexture2D();
        GL11.glEnable(GL11.GL_LINE_STIPPLE);
        color.applyGlColor();
        GL11.glLineWidth(width);
        GL11.glLineStipple(stipScale, stipMask);

        int x1 = start.getX() + start.getWidth() / 2;
        int y1 = start.getY() + start.getHeight() / 2;
        int x2 = end.getX() + end.getWidth() / 2;
        int y2 = end.getY() + end.getHeight() / 2;

        int x3 = x1 + Math.round((x2 - x1) * bias);
        int y3 = y1 + Math.round((y2 - y1) * bias);

        if (bias > 0F) {
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(x1, y1);
            if (isVertical) {
                GL11.glVertex2f(x1, y3);
            } else {
                GL11.glVertex2f(x3, y1);
            }
            GL11.glEnd();
        }

        if (isVertical) {
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(x1, y3);
            GL11.glVertex2f(x2, y3);
            GL11.glEnd();
        } else {
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(x3, y1);
            GL11.glVertex2f(x3, y2);
            GL11.glEnd();
        }

        if (bias < 1F) {
            GL11.glBegin(GL11.GL_LINES);
            if (isVertical) {
                GL11.glVertex2f(x2, y3);
            } else {
                GL11.glVertex2f(x3, y2);
            }
            GL11.glVertex2f(x2, y2);
            GL11.glEnd();
        }

        GL11.glLineStipple(1, (short) 0xFFFF);
        GL11.glLineWidth(1F);
        GL11.glDisable(GL11.GL_LINE_STIPPLE);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1F, 1F, 1F, 1F);

        GlStateManager.popMatrix();
    }
}
