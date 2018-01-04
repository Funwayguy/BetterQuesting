package betterquesting.api2.client.gui.resources;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class SimpleLine implements IGuiLine
{
	private final short pattern;
	private final int scale;
	
	public SimpleLine()
	{
		this(1, (short)0xFFFF);
	}
	
	public SimpleLine(int stippleScale, short stipplePattern)
	{
		this.pattern = stipplePattern;
		this.scale = 1;
	}
	
	@Override
	public void drawLine(int x1, int y1, int x2, int y2, int width, int color, float partialTick)
	{
		float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
		GlStateManager.pushMatrix();
		
		GlStateManager.disableTexture2D();
		GlStateManager.color(r, g, b, 1F);
		GL11.glLineWidth(width);
		GL11.glLineStipple(scale, pattern);
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(x1, y1);
		GL11.glVertex2f(x2, y2);
		GL11.glEnd();
		
		GL11.glLineStipple(1, (short)0xFFFF);
		GL11.glLineWidth(1F);
		GlStateManager.enableTexture2D();
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		GlStateManager.popMatrix();
	}
}
