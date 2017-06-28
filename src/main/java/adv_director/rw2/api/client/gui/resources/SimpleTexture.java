package adv_director.rw2.api.client.gui.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

public class SimpleTexture implements IGuiTexture
{
	private final ResourceLocation texture;
	private final Rectangle texBounds;
	
	public SimpleTexture(ResourceLocation texture, Rectangle bounds)
	{
		this.texture = texture;
		this.texBounds = bounds;
	}
	
	@Override
	public void drawTexture(int x, int y, int width, int height, float zLevel)
	{
		GlStateManager.pushMatrix();
		
		float sx = (float)width / (float)texBounds.getWidth();
		float sy = (float)height / (float)texBounds.getHeight();
		GlStateManager.translate(x, y, 0F);
		GlStateManager.scale(sx, sy, 1F);
		
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		GuiUtils.drawTexturedModalRect(0, 0, texBounds.getX(), texBounds.getY(), texBounds.getWidth(), texBounds.getHeight(), zLevel);
		
		GlStateManager.popMatrix();
	}
	
	@Override
	public ResourceLocation getTexture()
	{
		return this.texture;
	}
	
	@Override
	public Rectangle getBounds()
	{
		return this.texBounds;
	}
}
