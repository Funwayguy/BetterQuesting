package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import cpw.mods.fml.client.config.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class SimpleTexture implements IGuiTexture
{
	private static final IGuiColor defColor = new GuiColorStatic(255, 255, 255, 255);
	
	private final ResourceLocation texture;
	private final IGuiRect texBounds;
	private boolean maintainAspect = false;
	
	public SimpleTexture(ResourceLocation texture, IGuiRect bounds)
	{
		this.texture = texture;
		this.texBounds = bounds;
	}
	
	public SimpleTexture maintainAspect(boolean enable)
	{
		this.maintainAspect = enable;
		return this;
	}
	
	@Override
	public void drawTexture(int x, int y, int width, int height, float zLevel, float partialTick)
	{
		drawTexture(x, y, width, height, zLevel, partialTick, defColor);
	}
	
	@Override
	public void drawTexture(int x, int y, int width, int height, float zLevel, float partialTick, IGuiColor color)
	{
	    if(width <= 0 || height <= 0) return;
	    
		GL11.glPushMatrix();
		
		float sx = (float)width / (float)texBounds.getWidth();
		float sy = (float)height / (float)texBounds.getHeight();
		
		if(maintainAspect)
		{
			float sa = Math.min(sx, sy);
			float dx = (sx - sa) * texBounds.getWidth() / 2F;
			float dy = (sy - sa) * texBounds.getHeight() / 2F;
			sx = sa;
			sy = sa;
			GL11.glTranslatef(x + dx, y + dy, 0F);
		} else
		{
			GL11.glTranslatef(x, y, 0F);
		}
        
        GL11.glScalef(sx, sy, 1F);
		color.applyGlColor();
		
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		GuiUtils.drawTexturedModalRect(0, 0, texBounds.getX(), texBounds.getY(), texBounds.getWidth(), texBounds.getHeight(), zLevel);
        
        GL11.glPopMatrix();
	}
	
	@Override
	public ResourceLocation getTexture()
	{
		return this.texture;
	}
	
	@Override
	public IGuiRect getBounds()
	{
		return this.texBounds;
	}
}
