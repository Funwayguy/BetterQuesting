package betterquesting.api.utils;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.core.BetterQuesting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.vector.Matrix4f;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

// TODO: Move text related stuff to its own utility class
@SideOnly(Side.CLIENT)
public class RenderUtils
{
	public static final String REGEX_NUMBER = "[^\\.0123456789-]"; // I keep screwing this up so now it's reusable
    public static final RenderItem itemRender = new RenderItem();
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text)
	{
		RenderItemStack(mc, stack, x, y, 16F, text, 0xFFFFFFFF);
	}
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, Color color)
	{
		RenderItemStack(mc, stack, x, y, 16F, text, color.getRGB());
	}
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, int color)
	{
		RenderItemStack(mc, stack, x, y, 16F, text, color);
	}
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, float z, String text, int color)
	{
		if(stack == null) return;
		
		GL11.glPushMatrix();
	    float preZ = itemRender.zLevel;
		
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
		GL11.glColor3f(r, g, b);
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		GL11.glTranslatef(0.0F, 0.0F, z);
		itemRender.zLevel = -50F; // Counters internal Z depth change so that GL translation makes sense // NOTE: Slightly different depth in 1.7.10
  
		FontRenderer font = stack.getItem().getFontRenderer(stack);
		if (font == null) font = mc.fontRenderer;
		
		try
		{
		    itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), stack, x, y);
		    
		    if (stack.stackSize != 1 || text != null)
			{
				GL11.glPushMatrix();
				
				int w = getStringWidth(text, font);
				float tx;
				float ty;
				float s = 1F;
				
				if(w > 17)
				{
					s = 17F / w;
					tx = 0;
					ty = 17 - font.FONT_HEIGHT * s;
				} else
				{
					tx = 17 - w;
					ty = 18 - font.FONT_HEIGHT;
				}
				
				GL11.glTranslatef(x + tx, y + ty, 0);
				GL11.glScalef(s, s, 1F);
				
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glDisable(GL11.GL_BLEND);
				
				font.drawString(text, 0, 0, 16777215, true);
				
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				
		    	GL11.glPopMatrix();
			}
			
			itemRender.renderItemOverlayIntoGUI(font, mc.getTextureManager(), stack, x, y, "");
		} catch(Exception e)
		{
			BetterQuesting.logger.warn("Unabled to render item " + stack, e);
		}
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		RenderHelper.disableStandardItemLighting();
		
	    itemRender.zLevel = preZ; // Just in case
		
        GL11.glPopMatrix();
	}
	
    public static void RenderEntity(int posX, int posY, int scale, float rotation, float pitch, Entity entity)
    {
    	RenderEntity(posX, posY, 64F, scale, rotation, pitch, entity);
	}
	
    public static void RenderEntity(float posX, float posY, float posZ, int scale, float rotation, float pitch, Entity entity)
    {
    	try
    	{
	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	        GL11.glPushMatrix();
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
	        GL11.glTranslatef(posX, posY, posZ);
	        GL11.glScalef((float)-scale, (float)scale, (float)scale); // Not entirely sure why mobs are flipped but this is how vanilla GUIs fix it so...
	        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
	        GL11.glRotatef(pitch, 1F, 0F, 0F);
	        GL11.glRotatef(rotation, 0F, 1F, 0F);
	        float f3 = entity.rotationYaw;
	        float f4 = entity.rotationPitch;
	        RenderHelper.enableStandardItemLighting();
	        RenderManager.instance.playerViewY = 180.0F;
	        RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
	        entity.rotationYaw = f3;
	        entity.rotationPitch = f4;
	        GL11.glDisable(GL11.GL_DEPTH_TEST);
	        GL11.glPopMatrix();
	        RenderHelper.disableStandardItemLighting();
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	        GL11.glEnable(GL11.GL_TEXTURE_2D); // Breaks subsequent text rendering if not included
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
		GL11.glPushMatrix();
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(r, g, b, 1F);
		GL11.glLineWidth(width);
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(x1, y1);
		GL11.glVertex2f(x2, y2);
		GL11.glEnd();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		GL11.glPopMatrix();
	}
	
	public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow)
	{
		drawSplitString(renderer, string, x, y, width, color, shadow, 0, splitString(string, width, renderer).size() - 1);
	}
	
	public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int start, int end)
	{
		drawHighlightedSplitString(renderer, string, x, y, width, color, shadow, start, end, 0, 0, 0);
	}
	
	// TODO: Clean this up. The list of parameters is getting a bit excessive
	
	public static void drawHighlightedSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int highlightColor, int highlightStart, int highlightEnd)
	{
		drawHighlightedSplitString(renderer, string, x, y, width, color, shadow, 0, splitString(string, width, renderer).size() - 1, highlightColor, highlightStart, highlightEnd);
	}
	
	public static void drawHighlightedSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int start, int end, int highlightColor, int highlightStart, int highlightEnd)
	{
		if(renderer == null || string == null || string.length() <= 0 || start > end)
		{
			return;
		}
		
		string = string.replaceAll("\r", ""); //Line endings from localizations break things so we remove them
		
		List<String> list = splitString(string, width, renderer);
		List<String> noFormat = splitStringWithoutFormat(string, width, renderer); // Needed for accurate highlight index positions
		
		if(list.size() != noFormat.size())
		{
			//BetterQuesting.logger.error("Line count mismatch (" + list.size() + " != " + noFormat.size() + ") while drawing formatted text!");
			return;
		}
		
		int hlStart = Math.min(highlightStart, highlightEnd);
		int hlEnd = Math.max(highlightStart, highlightEnd);
		int idxStart = 0;
		
		for(int i = 0; i < start; i++)
		{
			if(i >= noFormat.size())
			{
				break;
			}
			
			idxStart += noFormat.get(i).length();
		}
		
		for(int i = start; i <= end; i++)
		{
			if(i < 0 || i >= list.size())
			{
				continue;
			}
			
			renderer.drawString(list.get(i), x, y + (renderer.FONT_HEIGHT * (i - start)), color, shadow);
			
			// DEBUG
			/*boolean b = (System.currentTimeMillis()/1000)%2 == 0;
			
			if(b)
			{
				renderer.drawString(i + ": " + list.get(i), x, y + (renderer.FONT_HEIGHT * (i - start)), color, shadow);
			}
			
			if(i >= noFormat.size())
			{
				continue;
			}
			
			if(!b)
			{
				renderer.drawString(i + ": " + noFormat.get(i), x, y + (renderer.FONT_HEIGHT * (i - start)), color, shadow);
			}*/
			
			int lineSize = noFormat.get(i).length();
			int idxEnd = idxStart + lineSize;
			
			int i1 = Math.max(idxStart, hlStart) - idxStart;
			int i2 = Math.min(idxEnd, hlEnd) - idxStart;
			
			if(!(i1 == i2 || i1 < 0 || i2 < 0 || i1 > lineSize || i2 > lineSize))
			{
				String lastFormat = getFormatFromString(list.get(i));
				int x1 = getStringWidth(lastFormat + noFormat.get(i).substring(0, i1), renderer);
				int x2 = getStringWidth(lastFormat + noFormat.get(i).substring(0, i2), renderer);
				
				drawHighlightBox(x + x1, y + (renderer.FONT_HEIGHT * (i - start)), x + x2, y + (renderer.FONT_HEIGHT * (i - start)) + renderer.FONT_HEIGHT, highlightColor);
			}
			
			idxStart = idxEnd;
		}
	}
	
	public static void drawHighlightedString(FontRenderer renderer, String string, int x, int y, int color, boolean shadow, int highlightColor, int highlightStart, int highlightEnd)
	{
		if(renderer == null || string == null || string.length() <= 0)
		{
			return;
		}
		
		renderer.drawString(string, x, y, color, shadow);
		
		int hlStart = Math.min(highlightStart, highlightEnd);
		int hlEnd = Math.max(highlightStart, highlightEnd);
		int size = string.length();
		
		int i1 = MathHelper.clamp_int(hlStart, 0, size);
		int i2 = MathHelper.clamp_int(hlEnd, 0, size);
		
		if(i1 != i2)
		{
			int x1 = getStringWidth(string.substring(0, i1), renderer);
			int x2 = getStringWidth(string.substring(0, i2), renderer);
			
			drawHighlightBox(x + x1, y, x + x2, y + renderer.FONT_HEIGHT, highlightColor);
		}
	}
	
	public static void drawHighlightBox(IGuiRect rect, IGuiColor color)
	{
		drawHighlightBox(rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), color.getRGB());
	}
	
	public static void drawHighlightBox(int left, int top, int right, int bottom, int color)
	{
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
		
		GL11.glPushMatrix();
  
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
        Tessellator tessellator = Tessellator.instance;
        //VertexBuffer bufferbuilder = tessellator.getBuffer();
        GL11.glColor4f(f, f1, f2, f3);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
       	GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glLogicOp(GL11.GL_OR_REVERSE);
        tessellator.startDrawingQuads();
        tessellator.addVertex((double)left, (double)bottom, 0.0D);
        tessellator.addVertex((double)right, (double)bottom, 0.0D);
        tessellator.addVertex((double)right, (double)top, 0.0D);
        tessellator.addVertex((double)left, (double)top, 0.0D);
        tessellator.draw();
        GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        
		GL11.glEnable(GL11.GL_TEXTURE_2D);
        
        GL11.glPopMatrix();
	}
    
    public static void drawColoredRect(IGuiRect rect, IGuiColor color)
    {
        Tessellator tessellator = Tessellator.instance;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        color.applyGlColor();
        tessellator.startDrawingQuads();
        tessellator.addVertex((double)rect.getX(), (double)rect.getY() + rect.getHeight(), 0.0D);
        tessellator.addVertex((double)rect.getX() + rect.getWidth(), (double)rect.getY() + rect.getHeight(), 0.0D);
        tessellator.addVertex((double)rect.getX() + rect.getWidth(), (double)rect.getY(), 0.0D);
        tessellator.addVertex((double)rect.getX(), (double)rect.getY(), 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
	
	private static final Stack<IGuiRect> scissorStack = new Stack<>();
	
	/**
	 * Performs a OpenGL scissor based on Minecraft's resolution instead of display resolution and adds it to the stack of ongoing scissors.
	 * Not using this method will result in incorrect scissoring and scaling of parent/child GUIs
	 */
	public static void startScissor(IGuiRect rect)
	{
		if(scissorStack.size() >= 255)
		{
			throw new IndexOutOfBoundsException("Exceeded the maximum number of nested scissor (255)");
		}
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution r = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		int f = r.getScaleFactor();
		
		// Have to do all this fancy stuff because glScissor() isn't affected by glScale() or glTranslate() and rather than try and convince devs to use some custom hack
		// we'll just deal with it by reading from the current MODELVIEW MATRIX to convert between screen spaces at their relative scales and translations.
		FloatBuffer fb = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, fb);
		fb.rewind();
		Matrix4f fm = new Matrix4f();
		fm.load(fb);
		
		// GL screenspace rectangle
		GuiRectangle sRect = new GuiRectangle((int)(rect.getX() * f  * fm.m00 + (fm.m30 * f)), (r.getScaledHeight() - (int)((rect.getY() + rect.getHeight()) * fm.m11 + fm.m31)) * f, (int)(rect.getWidth() * f * fm.m00), (int)(rect.getHeight() * f * fm.m11));
		
		if(!scissorStack.empty())
		{
			IGuiRect parentRect = scissorStack.peek();
			int x = Math.max(parentRect.getX(), sRect.getX());
			int y = Math.max(parentRect.getY(), sRect.getY());
			int w = Math.min(parentRect.getX() + parentRect.getWidth(), sRect.getX() + sRect.getWidth());
			int h = Math.min(parentRect.getY() + parentRect.getHeight(), sRect.getY() + sRect.getHeight());
			w = Math.max(0, w - x); // Clamp to 0 to prevent OpenGL errors
			h = Math.max(0, h - y); // Clamp to 0 to prevent OpenGL errors
			sRect = new GuiRectangle(x, y, w, h, 0);
		} else
        {
            sRect.w = Math.max(0, sRect.w);
            sRect.h = Math.max(0, sRect.h);
        }
		
		GL11.glScissor(sRect.getX(),sRect.getY(), sRect.getWidth(), sRect.getHeight());
		scissorStack.add(sRect);
	}
	
	/**
	 * Pops the last scissor off the stack and returns to the last parent scissor or disables it if there are none
	 */
	public static void endScissor()
	{
		scissorStack.pop();
		
		if(scissorStack.empty())
		{
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		} else
		{
			IGuiRect rect = scissorStack.peek();
			GL11.glScissor(rect.getX(),rect.getY(), rect.getWidth(), rect.getHeight());
		}
	}
	
	/**
	 * Similar to normally splitting a string with the fontRenderer however this variant does
	 * not attempt to preserve the formatting between lines. This is particularly important when the
	 * index positions in the text are required to match the original unwrapped text.
	 */
	public static List<String> splitStringWithoutFormat(String str, int wrapWidth, FontRenderer font)
	{
		List<String> list = new ArrayList<>();
		
		String lastFormat = ""; // Formatting like bold can affect the wrapping width
		String temp = str;
		
		while(true)
		{
			int i = sizeStringToWidth(lastFormat + temp, wrapWidth, font); // Cut to size WITH formatting
			i -= lastFormat.length(); // Remove formatting characters from count
			
			if(temp.length() <= i)
			{
				list.add(temp);
				break;
			} else
			{
				String s = temp.substring(0, i);
				char c0 = temp.charAt(i);
				boolean flag = c0 == ' ' || c0 == '\n';
				lastFormat = getFormatFromString(lastFormat + s);
				temp = temp.substring(i + (flag ? 1 : 0));
				// NOTE: The index actually stops just before the space/nl so we don't need to remove it from THIS line. This is why the previous line moves forward by one for the NEXT line
				list.add(s + (flag ? "\n" : "")); // Although we need to remove the spaces between each line we have to replace them with invisible new line characters to preserve the index count
				
				if(temp.length() <= 0 && !flag)
				{
					break;
				}
			}
		}
        
        return list;
	}
	
	public static List<String> splitString(String str, int wrapWidth, FontRenderer font)
	{
		List<String> list = new ArrayList<>();
		
		String temp = str;
		
		while(true)
		{
			int i = sizeStringToWidth(temp, wrapWidth, font); // Cut to size WITH formatting
			
			if(temp.length() <= i)
			{
				list.add(temp);
				break;
			} else
			{
				String s = temp.substring(0, i);
				char c0 = temp.charAt(i);
				boolean flag = c0 == ' ' || c0 == '\n';
				temp = getFormatFromString(s) + temp.substring(i + (flag ? 1 : 0));
				list.add(s);
				
				if(temp.length() <= 0 && !flag)
				{
					break;
				}
			}
		}
        
        return list;
	}
	
	/**
	 * Returns the index position under a given set of coordinates in a piece of text
	 */
	public static int getCursorPos(String text, int x, FontRenderer font)
	{
	    if(text.length() <= 0)
        {
            return 0;
        }
        
		int i = 0;
		
		for(; i < text.length(); i++)
		{
			if(getStringWidth(text.substring(0, i + 1), font) > x)
			{
				break;
			}
		}
		
		if(i - 1 >= 0 && text.charAt(i - 1) == '\n')
        {
            return i - 1;
        }
		
		return i;
	}
	
	/**
	 * Returns the index position under a given set of coordinates in a wrapped piece of text
	 */
	public static int getCursorPos(String text, int x, int y, int width, FontRenderer font)
	{
		List<String> tLines = RenderUtils.splitStringWithoutFormat(text, width, font);
		
		if(tLines.size() <= 0)
		{
			return 0;
		}
		
		int row = MathHelper.clamp_int(y/font.FONT_HEIGHT, 0, tLines.size() - 1);
		String lastFormat = "";
		String line;
		int idx = 0;
		
		for(int i = 0; i < row; i++)
		{
			line = tLines.get(i);
			idx += line.length();
			lastFormat = getFormatFromString(lastFormat + line);
		}
		
		return idx + getCursorPos(lastFormat + tLines.get(row), x, font) - lastFormat.length();
	}
    
    public static String getFormatFromString(String p_78282_0_)
    {
        String s1 = "";
        int i = -1;
        int j = p_78282_0_.length();

        while ((i = p_78282_0_.indexOf(167, i + 1)) != -1)
        {
            if (i < j - 1)
            {
                char c0 = p_78282_0_.charAt(i + 1);

                if (isFormatColor(c0))
                {
                    s1 = "\u00a7" + c0;
                }
                else if (isFormatSpecial(c0))
                {
                    s1 = s1 + "\u00a7" + c0;
                }
            }
        }

        return s1;
    }
	
    private static int sizeStringToWidth(String str, int wrapWidth, FontRenderer font)
    {
        int i = str.length();
        int j = 0;
        int k = 0;
        int l = -1;

        for (boolean flag = false; k < i; ++k)
        {
            char c0 = str.charAt(k);

            switch (c0)
            {
                case '\n':
                    --k;
                    break;
                case ' ':
                    l = k;
                default:
                    j += font.getCharWidth(c0);

                    if (flag)
                    {
                        ++j;
                    }

                    break;
                case '\u00a7':

                    if (k < i - 1)
                    {
                        ++k;
                        char c1 = str.charAt(k);

                        if (c1 != 'l' && c1 != 'L')
                        {
                            if (c1 == 'r' || c1 == 'R' || isFormatColor(c1))
                            {
                                flag = false;
                            }
                        }
                        else
                        {
                            flag = true;
                        }
                    }
            }

            if (c0 == '\n')
            {
                ++k;
                l = k;
                break;
            }

            if (j > wrapWidth)
            {
                break;
            }
        }

        return k != i && l != -1 && l < k ? l : k;
    }
    
    private static boolean isFormatColor(char colorChar)
    {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }
    
    private static boolean isFormatSpecial(char p_78270_0_)
    {
        return p_78270_0_ >= 107 && p_78270_0_ <= 111 || p_78270_0_ >= 75 && p_78270_0_ <= 79 || p_78270_0_ == 114 || p_78270_0_ == 82;
    }
    
	public static float lerpFloat(float f1, float f2, float blend)
	{
		return (f2 * blend) + (f1 * (1F - blend));
	}
 
	public static double lerpDouble(double d1, double d2, double blend)
	{
		return (d2 * blend) + (d1 * (1D - blend));
	}
	
	public static int lerpRGB(int c1, int c2, float blend)
	{
		float a1 = c1 >> 24 & 255;
		float r1 = c1 >> 16 & 255;
		float g1 = c1 >> 8 & 255;
		float b1 = c1 & 255;
		
		float a2 = c2 >> 24 & 255;
		float r2 = c2 >> 16 & 255;
		float g2 = c2 >> 8 & 255;
		float b2 = c2 & 255;
		
		int a3 = (int)lerpFloat(a1, a2, blend);
		int r3 = (int)lerpFloat(r1, r2, blend);
		int g3 = (int)lerpFloat(g1, g2, blend);
		int b3 = (int)lerpFloat(b1, b2, blend);
		
		return (a3 << 24) + (r3 << 16) + (g3 << 8) + b3;
	}
	
    public static void drawHoveringText(List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font)
    {
        drawHoveringText(null, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, font);
    }
	
	/**
	 * Modified version of Forge's tooltip rendering that doesn't adjust Z depth
	 */
    public static void drawHoveringText(final ItemStack stack, List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font)
    {
    	if(textLines == null || textLines.isEmpty())
		{
			return;
		}
    
        GL11.glPushMatrix();
		GL11.glTranslatef(0F, 0F, 32F);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
		//GlStateManager.enableDepth();
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		int tooltipTextWidth = 0;

		for (String textLine : textLines)
		{
			int textLineWidth = getStringWidth(textLine, font);

			if (textLineWidth > tooltipTextWidth)
			{
				tooltipTextWidth = textLineWidth;
			}
		}

		boolean needsWrap = false;

		int titleLinesCount = 1;
		int tooltipX = mouseX + 12;
		
		if (tooltipX + tooltipTextWidth + 4 > screenWidth)
		{
			tooltipX = mouseX - 16 - tooltipTextWidth;
			
			if (tooltipX < 4) // if the tooltip doesn't fit on the screen
			{
				if (mouseX > screenWidth / 2)
				{
					tooltipTextWidth = mouseX - 12 - 8;
				}
				else
				{
					tooltipTextWidth = screenWidth - 16 - mouseX;
				}
				needsWrap = true;
			}
		}

		if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth)
		{
			tooltipTextWidth = maxTextWidth;
			needsWrap = true;
		}

		if (needsWrap)
		{
			int wrappedTooltipWidth = 0;
			List<String> wrappedTextLines = new ArrayList<>();
			
			for (int i = 0; i < textLines.size(); i++)
			{
				String textLine = textLines.get(i);
				List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);
				if (i == 0)
				{
					titleLinesCount = wrappedLine.size();
				}

				for (String line : wrappedLine)
				{
					int lineWidth = getStringWidth(line, font);
					if (lineWidth > wrappedTooltipWidth)
					{
						wrappedTooltipWidth = lineWidth;
					}
					wrappedTextLines.add(line);
				}
			}
			
			tooltipTextWidth = wrappedTooltipWidth;
			textLines = wrappedTextLines;

			if (mouseX > screenWidth / 2)
			{
				tooltipX = mouseX - 16 - tooltipTextWidth;
			}
			else
			{
				tooltipX = mouseX + 12;
			}
		}

		int tooltipY = mouseY - 12;
		int tooltipHeight = 8;

		if (textLines.size() > 1)
		{
			tooltipHeight += (textLines.size() - 1) * 10;
			
			if (textLines.size() > titleLinesCount)
			{
				tooltipHeight += 2; // gap between title lines and next lines
			}
		}

		if (tooltipY < 4)
		{
			tooltipY = 4;
		} else if (tooltipY + tooltipHeight + 4 > screenHeight)
		{
			tooltipY = screenHeight - tooltipHeight - 4;
		}
		
		int backgroundColor = 0xF0100010;
		int borderColorStart = 0x505000FF;
		int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
		
		drawGradientRect(0, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
		drawGradientRect(0, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
		drawGradientRect(0, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		drawGradientRect(0, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		drawGradientRect(0, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		drawGradientRect(0, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		drawGradientRect(0, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		drawGradientRect(0, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
		drawGradientRect(0, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);
  
		int tooltipTop = tooltipY;
		
		GL11.glTranslatef(0F, 0F, 0.1F);

		for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber)
		{
			String line = textLines.get(lineNumber);
			font.drawStringWithShadow(line, tooltipX, tooltipY, -1);

			if (lineNumber + 1 == titleLinesCount)
			{
				tooltipY += 2;
			}

			tooltipY += 10;
		}
    
        GL11.glEnable(GL11.GL_LIGHTING);
		//GlStateManager.disableDepth();
		//GlStateManager.enableDepth();
		RenderHelper.enableStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
    }

    public static void drawGradientRect(int zDepth, int p_drawGradientRect_1_, int p_drawGradientRect_2_, int p_drawGradientRect_3_, int p_drawGradientRect_4_, int p_drawGradientRect_5_, int p_drawGradientRect_6_)
    {
        float var7 = (float)(p_drawGradientRect_5_ >> 24 & 255) / 255.0F;
        float var8 = (float)(p_drawGradientRect_5_ >> 16 & 255) / 255.0F;
        float var9 = (float)(p_drawGradientRect_5_ >> 8 & 255) / 255.0F;
        float var10 = (float)(p_drawGradientRect_5_ & 255) / 255.0F;
        float var11 = (float)(p_drawGradientRect_6_ >> 24 & 255) / 255.0F;
        float var12 = (float)(p_drawGradientRect_6_ >> 16 & 255) / 255.0F;
        float var13 = (float)(p_drawGradientRect_6_ >> 8 & 255) / 255.0F;
        float var14 = (float)(p_drawGradientRect_6_ & 255) / 255.0F;
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glDisable(3008);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(7425);
        Tessellator var15 = Tessellator.instance;
        var15.startDrawingQuads();
        var15.setColorRGBA_F(var8, var9, var10, var7);
        var15.addVertex((double)p_drawGradientRect_3_, (double)p_drawGradientRect_2_, (double)zDepth);
        var15.addVertex((double)p_drawGradientRect_1_, (double)p_drawGradientRect_2_, (double)zDepth);
        var15.setColorRGBA_F(var12, var13, var14, var11);
        var15.addVertex((double)p_drawGradientRect_1_, (double)p_drawGradientRect_4_, (double)zDepth);
        var15.addVertex((double)p_drawGradientRect_3_, (double)p_drawGradientRect_4_, (double)zDepth);
        var15.draw();
        GL11.glShadeModel(7424);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glEnable(3553);
    }
    
    /**
     *  A version of getStringWidth that actually behaves according to the format resetting rules of colour codes. Minecraft's built in one is busted!
     */
    public static int getStringWidth(String text, FontRenderer font)
    {
        if (text == null || text.length() == 0) return 0;
        
        int i = 0;
        boolean flag = false;

        for (int j = 0; j < text.length(); ++j)
        {
            char c0 = text.charAt(j);
            int k = font.getCharWidth(c0);

            if (k < 0 && j < text.length() - 1) // k should only be negative when the section sign has been used!
            {
                ++j;
                c0 = text.charAt(j);

                if (c0 != 'l' && c0 != 'L')
                {
                    int ci = "0123456789abcdefklmnor".indexOf(String.valueOf(c0).toLowerCase(Locale.ROOT).charAt(0));
                    //if (c0 == 'r' || c0 == 'R') // Minecraft's original implemention. This is broken...
                    if(ci < 16 || ci == 21) // Colour or reset code!
                    {
                        flag = false;
                    }
                }
                else
                {
                    flag = true;
                }

                k = 0;
            }

            i += k;

            if (flag && k > 0)
            {
                ++i;
            }
        }

        return i;
    }
}
