package betterquesting.utils;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;

@SideOnly(Side.CLIENT)
public class RenderUtils
{
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text)
	{
		Color c = new Color(1F, 1F, 1F, 1F);
		RenderItemStack(mc, stack, x, y, text, c);
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
			
			stack.getItem().getSubItems(stack.getItem(), CreativeTabs.tabAllSearch, tmp);
			
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
		    GlStateManager.color(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F, 1F);
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
	        rendermanager.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
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
	
	public static void DrawLine(int x1, int y1, int x2, int y2, float width, Color color)
	{
		GlStateManager.pushMatrix();
		
		GlStateManager.disableTexture2D();
		GlStateManager.color(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F, color.getAlpha()/255F);
		GL11.glLineWidth(width);
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(x1, y1);
		GL11.glVertex2f(x2, y2);
		GL11.glEnd();
		
		GlStateManager.enableTexture2D();
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		GlStateManager.popMatrix();
	}
	
	public static void DrawFakeButton(GuiQuesting screen, int x, int y, int width, int height, String text, int state)
	{
        FontRenderer fontrenderer = screen.mc.fontRendererObj;
        screen.mc.getTextureManager().bindTexture(ThemeRegistry.curTheme().guiTexture());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.blendFunc(770, 771);
        screen.drawTexturedModalRect(x, y, 48, state * 20, width / 2, height);
        screen.drawTexturedModalRect(x + width / 2, y, 248 - width / 2, state * 20, width / 2, height);
        int l = 14737632;

        if (state == 0)
        {
            l = 10526880;
        }
        else if (state == 2)
        {
            l = 16777120;
        }

        screen.drawCenteredString(fontrenderer, text, x + width / 2, y + (height - 8) / 2, l);
        GlStateManager.color(1F, 1F, 1F, 1F);
	}
	
	static Method bidiReorder;
	static Method stringAtPos;
	static Method resetStyle;
	static Field textColor;
	static Field red;
	static Field green;
	static Field blue;
	static Field alpha;
	static Field rx;
	static Field ry;
	
	/**
	 * Fixed version of the one in FontRenderer. Supports multi-line color formatting (lots of reflection hacking involved!)
	 */
	public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean isShadow)
	{
		drawSplitString(renderer, string, x, y, width, color, isShadow, 0, renderer.listFormattedStringToWidth(string, width).size() - 1);
	}
	
	public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean isShadow, int start, int end)
	{
		if(renderer == null || string == null || string.length() <= 0)
		{
			return;
		}
		
		string = string.replaceAll("\r", ""); //Line endings from localisation break things so we remove them
		
		// Pre-render setup
		if(bidiReorder == null || stringAtPos == null || resetStyle == null || textColor == null || red == null || green == null || blue == null || alpha == null || rx == null || ry == null)
		{
			try
			{
				bidiReorder = FontRenderer.class.getDeclaredMethod("bidiReorder", String.class);
				bidiReorder.setAccessible(true);
				stringAtPos = FontRenderer.class.getDeclaredMethod("renderStringAtPos", String.class, boolean.class);
				stringAtPos.setAccessible(true);
				resetStyle = FontRenderer.class.getDeclaredMethod("resetStyles");
				resetStyle.setAccessible(true);
				textColor = FontRenderer.class.getDeclaredField("textColor");
				textColor.setAccessible(true);
				red = FontRenderer.class.getDeclaredField("red");
				red.setAccessible(true);
				green = FontRenderer.class.getDeclaredField("green");
				green.setAccessible(true);
				blue = FontRenderer.class.getDeclaredField("blue");
				blue.setAccessible(true);
				alpha = FontRenderer.class.getDeclaredField("alpha");
				alpha.setAccessible(true);
				rx = FontRenderer.class.getDeclaredField("posX");
				rx.setAccessible(true);
				ry = FontRenderer.class.getDeclaredField("posY");
				ry.setAccessible(true);
			} catch(Exception e)
			{
				try
				{
					bidiReorder = FontRenderer.class.getDeclaredMethod("func_147647_b", String.class);
					bidiReorder.setAccessible(true);
					stringAtPos = FontRenderer.class.getDeclaredMethod("func_78255_a", String.class, boolean.class);
					stringAtPos.setAccessible(true);
					resetStyle = FontRenderer.class.getDeclaredMethod("func_78265_b");
					resetStyle.setAccessible(true);
					textColor = FontRenderer.class.getDeclaredField("field_78304_r");
					textColor.setAccessible(true);
					red = FontRenderer.class.getDeclaredField("field_78291_n");
					red.setAccessible(true);
					green = FontRenderer.class.getDeclaredField("field_78306_p");
					green.setAccessible(true);
					blue = FontRenderer.class.getDeclaredField("field_78292_o");
					blue.setAccessible(true);
					alpha = FontRenderer.class.getDeclaredField("field_78305_q");
					alpha.setAccessible(true);
					rx = FontRenderer.class.getDeclaredField("field_78295_j");
					rx.setAccessible(true);
					ry = FontRenderer.class.getDeclaredField("field_78296_k");
					ry.setAccessible(true);
				} catch(Exception e2)
				{
					BetterQuesting.logger.log(Level.ERROR, "Unable to render split string: ", e);
					return;
				}
			}
		}
		
		// Pre-render split
		try
		{
	        resetStyle.invoke(renderer);
	        textColor.set(renderer, color);
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to render split string: ", e);
			return;
		}
        
        while (string != null && string.endsWith("\n"))
        {
            string = string.substring(0, string.length() - 1); // Remove trailing new lines
        }
        
		// Render split

        List<String> list = renderer.listFormattedStringToWidth(string, width);

        for (int i = 0; i < list.size() && i <= end; i++)
        {
            String s1 = list.get(i);
            
            // Render aligned
            if (renderer.getBidiFlag())
            {
            	try
            	{
	                int i1 = renderer.getStringWidth((String)bidiReorder.invoke(renderer, s1));
	                x = x + width - i1;
            	} catch(Exception e)
            	{
        			BetterQuesting.logger.log(Level.ERROR, "Unable to render split string: ", e);
        			return;
            	}
            }
            
            // Render string
            if (renderer.getBidiFlag())
            {
            	try
            	{
            		s1 = (String)bidiReorder.invoke(renderer, s1);
            	} catch(Exception e)
            	{
        			BetterQuesting.logger.log(Level.ERROR, "Unable to render split string: ", e);
        			return;
            	}
            }

            if ((color & -67108864) == 0)
            {
                color |= -16777216;
            }

            if (isShadow)
            {
                color = (color & 16579836) >> 2 | color & -16777216;
            }

            float r = (float)(color >> 16 & 255) / 255.0F;
            float b = (float)(color >> 8 & 255) / 255.0F;
            float g = (float)(color & 255) / 255.0F;
            float a = (float)(color >> 24 & 255) / 255.0F;
            try
            {
	            red.set(renderer, r);
	            green.set(renderer, g);
	            blue.set(renderer, b);
	            alpha.set(renderer, a);
	            //setColor(this.red, this.blue, this.green, this.alpha);
	            GlStateManager.color(r, g, b, a);
	            rx.set(renderer, x);
	            ry.set(renderer, y);
	            
	            if(i >= start) // We check here instead to preserve prior formatting
	            {
		            stringAtPos.invoke(renderer, s1, isShadow);
		            y += renderer.FONT_HEIGHT;
	            }
            } catch(Exception e)
            {
    			BetterQuesting.logger.log(Level.ERROR, "Unable to render split string: ", e);
    			return;
            }
        }
	}
}
