package betterquesting.api.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderUtils
{
	public static final String REGEX_NUMBER = "[^\\.0123456789-]"; // I keep screwing this up so now it's reusable
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text)
	{
		RenderItemStack(mc, stack, x, y, text, Color.WHITE);
	}
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, int color)
	{
		float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
		RenderItemStack(mc, stack, x, y, text, new Color(r, g, b));
	}
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, Color color)
	{
		if(stack == null || stack.getItem() == null)
		{
			return;
		}
		
		ItemStack rStack = stack;
		
		if(stack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
		{
			ArrayList<ItemStack> tmp = new ArrayList<ItemStack>();
			
			stack.getItem().getSubItems(stack.getItem(), CreativeTabs.SEARCH, tmp);
			
			if(tmp.size() > 0)
			{
				rStack = tmp.get((int)((Minecraft.getSystemTime()/1000)%tmp.size()));
			}
		}
		
		GlStateManager.pushMatrix();
		RenderItem itemRender = mc.getRenderItem();
	    float preZ = itemRender.zLevel;
        
		try
		{
		    GlStateManager.color(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F);
			RenderHelper.enableGUIStandardItemLighting();
		    GlStateManager.enableRescaleNormal();
			
		    GlStateManager.translate(0.0F, 0.0F, 32.0F);
		    itemRender.zLevel = 200.0F;
		    FontRenderer font = null;
		    if (rStack != null) font = rStack.getItem().getFontRenderer(rStack);
		    if (font == null) font = mc.fontRendererObj;
		    itemRender.renderItemAndEffectIntoGUI(rStack, x, y);
		    itemRender.renderItemOverlayIntoGUI(font, rStack, x, y, text);
		    
		    RenderHelper.disableStandardItemLighting();
		} catch(Exception e)
		{
		}
		
	    itemRender.zLevel = preZ; // Just in case
		
        GlStateManager.popMatrix();
	}

    public static void RenderEntity(int posX, int posY, int scale, float rotation, float pitch, Entity entity)
    {
    	try
    	{
	        GlStateManager.enableColorMaterial();
	        GlStateManager.pushMatrix();
	        GlStateManager.enableDepth();
	        GlStateManager.translate((float)posX, (float)posY, 100.0F);
	        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
	        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
	        GlStateManager.rotate(15F, 1F, 0F, 0F);
	        GlStateManager.rotate(rotation, 0F, 1F, 0F);
	        float f3 = entity.rotationYaw;
	        float f4 = entity.rotationPitch;
	        RenderHelper.enableStandardItemLighting();
	        GlStateManager.translate(0D, entity.getYOffset(), 0D);
	        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
	        rendermanager.setPlayerViewY(180.0F);
	        rendermanager.doRenderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
	        entity.rotationYaw = f3;
	        entity.rotationPitch = f4;
	        GlStateManager.popMatrix();
	        RenderHelper.disableStandardItemLighting();
	        GlStateManager.disableRescaleNormal();
	        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	        GlStateManager.disableTexture2D();
	        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    	} catch(Exception e)
    	{
    		// Hides rendering errors with entities which are common for invalid/technical entities
    	}
    }
	
	public static void DrawLine(int x1, int y1, int x2, int y2, float width, int color)
	{
		float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
		GlStateManager.pushMatrix();
		
		GlStateManager.disableTexture2D();
		GlStateManager.color(r, g, b, 1F);
		GL11.glLineWidth(width);
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(x1, y1);
		GL11.glVertex2f(x2, y2);
		GL11.glEnd();
		
		GlStateManager.enableTexture2D();
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		GlStateManager.popMatrix();
	}
	
	public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow)
	{
		drawSplitString(renderer, string, x, y, width, color, shadow, 0, renderer.listFormattedStringToWidth(string, width).size() - 1);
	}
	
	public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int start, int end)
	{
		if(renderer == null || string == null || string.length() <= 0 || start > end)
		{
			return;
		}
		
		string = string.replaceAll("\r", ""); //Line endings from localizations break things so we remove them
		
		List<String> list = renderer.listFormattedStringToWidth(string, width);
		
		for(int i = start; i <= end; i++)
		{
			if(i < 0 || i >= list.size())
			{
				continue;
			}
			
			renderer.drawString(list.get(i), x, y + (renderer.FONT_HEIGHT * (i - start)), color, shadow);
		}
	}
	
	/**
	 * Performs a OpenGL scissor based on Minecraft's resolution instead of display resolution
	 */
	public static void guiScissor(Minecraft mc, int x, int y, int w, int h)
	{
		ScaledResolution r = new ScaledResolution(mc);
		int f = r.getScaleFactor();
		
		GL11.glScissor(x * f, (r.getScaledHeight() - y - h)*f, w * f, h * f);
	}
}
