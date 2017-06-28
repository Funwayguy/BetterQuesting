package adv_director.rw2.api.client.gui.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;
import com.google.gson.JsonObject;
import adv_director.api.utils.JsonHelper;
import adv_director.rw2.api.client.gui.misc.GuiPadding;

public class SlicedTexture implements IGuiTexture
{
	private final ResourceLocation texture;
	private final Rectangle texBounds;
	private final GuiPadding texBorder;
	private int sliceMode = 1;
	
	public SlicedTexture(ResourceLocation tex, Rectangle bounds, GuiPadding border)
	{
		this.texture = tex;
		this.texBounds = bounds;
		this.texBorder = border;
	}
	
	@Override
	public void drawTexture(int x, int y, int width, int height, float zLevel)
	{
		GlStateManager.pushMatrix();
		
		if(sliceMode == 1 || sliceMode == 2)
		{
			drawContinuousTexturedBox(texture, x, y, texBounds.getX(), texBounds.getY(), width, height, texBounds.getWidth(), texBounds.getHeight(), texBorder.getTop(), texBorder.getBottom(), texBorder.getLeft(), texBorder.getRight(), zLevel);
			
			if(sliceMode == 2)
			{
				int iu = texBounds.getX() + texBorder.getLeft();
				int iv = texBounds.getY() + texBorder.getTop();
				int iw = texBounds.getWidth() - texBorder.getLeft() - texBorder.getRight();
				int ih = texBounds.getHeight() - texBorder.getTop() - texBorder.getBottom();
				
				float sx = (float)(width - (texBounds.getWidth() - iw)) / (float)iw;
				float sy = (float)(height - (texBounds.getHeight() - ih)) / (float)ih;
				GlStateManager.translate(x + texBorder.getLeft(), y + texBorder.getTop(), 0F);
				GlStateManager.scale(sx, sy, 1F);
				
				
				Minecraft.getMinecraft().renderEngine.bindTexture(texture);
				GuiUtils.drawTexturedModalRect(0, 0, iu, iv, iw, ih, zLevel);
			}
		} else
		{
			float sx = (float)width / (float)texBounds.getWidth();
			float sy = (float)height / (float)texBounds.getHeight();
			GlStateManager.translate(x, y, 0F);
			GlStateManager.scale(sx, sy, 1F);
			
	        GlStateManager.enableBlend();
	        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			
			Minecraft.getMinecraft().renderEngine.bindTexture(texture);
			GuiUtils.drawTexturedModalRect(0, 0, texBounds.getX(), texBounds.getY(), texBounds.getWidth(), texBounds.getHeight(), zLevel);
		}
		
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
	
	public GuiPadding getBorder()
	{
		return this.texBorder;
	}
	
	/**
	 * Enables texture slicing. Will stretch to fit if disabled
	 */
	public SlicedTexture setSliceMode(int mode)
	{
		this.sliceMode = mode;
		return this;
	}
	
	public static SlicedTexture readFromJson(JsonObject json)
	{
		ResourceLocation res = new ResourceLocation(JsonHelper.GetString(json, "texture", "minecraft:missingno"));
		int slice = JsonHelper.GetNumber(json, "sliceMode", 1).intValue();
		
		JsonObject jOut = JsonHelper.GetObject(json, "coordinates");
		int ox = JsonHelper.GetNumber(jOut, "u", 0).intValue();
		int oy = JsonHelper.GetNumber(jOut, "v", 0).intValue();
		int ow = JsonHelper.GetNumber(jOut, "w", 48).intValue();
		int oh = JsonHelper.GetNumber(jOut, "h", 48).intValue();
		
		JsonObject jIn = JsonHelper.GetObject(json, "border");
		int il = JsonHelper.GetNumber(jIn, "l", 16).intValue();
		int it = JsonHelper.GetNumber(jIn, "t", 16).intValue();
		int ir = JsonHelper.GetNumber(jIn, "r", 16).intValue();
		int ib = JsonHelper.GetNumber(jIn, "b", 16).intValue();
		
		return new SlicedTexture(res, new Rectangle(ox, oy, ow, oh), new GuiPadding(il, it, ir, ib)).setSliceMode(slice);
	}
	
	// Slightly modified version from GuiUtils.class
	private static void drawContinuousTexturedBox(ResourceLocation res, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(res);
		
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		
		int fillerWidth = textureWidth - leftBorder - rightBorder;
		int fillerHeight = textureHeight - topBorder - bottomBorder;
		int canvasWidth = width - leftBorder - rightBorder;
		int canvasHeight = height - topBorder - bottomBorder;
		int xPasses = canvasWidth / fillerWidth;
		int remainderWidth = canvasWidth % fillerWidth;
		int yPasses = canvasHeight / fillerHeight;
		int remainderHeight = canvasHeight % fillerHeight;
		
		// Draw Border
		// Top Left
		GuiUtils.drawTexturedModalRect(x, y, u, v, leftBorder, topBorder, zLevel);
		// Top Right
		GuiUtils.drawTexturedModalRect(x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder, zLevel);
		// Bottom Left
		GuiUtils.drawTexturedModalRect(x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder, zLevel);
		// Bottom Right
		GuiUtils.drawTexturedModalRect(x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder, zLevel);
		
		for(int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++)
		{
			// Top Border
			GuiUtils.drawTexturedModalRect(x + leftBorder + (i * fillerWidth), y, u + leftBorder, v, (i == xPasses ? remainderWidth : fillerWidth), topBorder, zLevel);
			// Bottom Border
			GuiUtils.drawTexturedModalRect(x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, (i == xPasses ? remainderWidth : fillerWidth), bottomBorder, zLevel);
			
			// Throw in some filler for good measure
			for(int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
				GuiUtils.drawTexturedModalRect(x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, (i == xPasses ? remainderWidth : fillerWidth), (j == yPasses ? remainderHeight : fillerHeight), zLevel);
		}
		
		// Side Borders
		for(int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
		{
			// Left Border
			GuiUtils.drawTexturedModalRect(x, y + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
			// Right Border
			GuiUtils.drawTexturedModalRect(x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
		}
	}
}
