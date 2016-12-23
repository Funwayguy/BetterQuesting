package betterquesting.api.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderUtils
{
	public static final String REGEX_NUMBER = "[^\\.0123456789-]"; // I keep screwing this up so now it's reusable
	public static RenderItem itemRender = new RenderItem();
	
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
			
			stack.getItem().getSubItems(stack.getItem(), CreativeTabs.tabAllSearch, tmp);
			
			if(tmp.size() > 0)
			{
				rStack = tmp.get((int)((Minecraft.getSystemTime()/1000)%tmp.size()));
			}
		}
		
		GL11.glPushMatrix();
		
		float preZ = itemRender.zLevel;
        
		try
		{
		    GL11.glColor3b((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue());
			RenderHelper.enableGUIStandardItemLighting();
		    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			
		    GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		    itemRender.zLevel = 200.0F;
		    FontRenderer font = null;
		    if (rStack != null) font = rStack.getItem().getFontRenderer(rStack);
		    if (font == null) font = mc.fontRenderer;
		    itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), rStack, x, y);
		    itemRender.renderItemOverlayIntoGUI(font, mc.getTextureManager(), rStack, x, y, text);
		    
		    RenderHelper.disableStandardItemLighting();
		} catch(Exception e)
		{
		}
		
		itemRender.zLevel = preZ;
		
        GL11.glPopMatrix();
	}

    public static void RenderEntity(int posX, int posY, int scale, float rotation, float pitch, Entity entity)
    {
    	try
    	{
	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	        GL11.glPushMatrix();
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
	        GL11.glTranslatef((float)posX, (float)posY, 100.0F);
	        GL11.glScalef((float)(-scale), (float)scale, (float)scale);
	        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
	        GL11.glRotatef(15F, 1F, 0F, 0F);
	        GL11.glRotatef(rotation, 0F, 1F, 0F);
	        float f3 = entity.rotationYaw;
	        float f4 = entity.rotationPitch;
	        RenderHelper.enableStandardItemLighting();
	        GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
	        RenderManager.instance.playerViewY = 180.0F;
	        RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
	        entity.rotationYaw = f3;
	        entity.rotationPitch = f4;
	        GL11.glPopMatrix();
	        RenderHelper.disableStandardItemLighting();
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
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
        
		GL11.glPushMatrix();
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(r, g, b);
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
		drawSplitString(renderer, string, x, y, width, color, shadow, 0, renderer.listFormattedStringToWidth(string, width).size() - 1);
	}
	
	public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int start, int end)
	{
		if(renderer == null || string == null || string.length() <= 0 || start > end)
		{
			return;
		}
		
		string = string.replaceAll("\r", ""); //Line endings from localizations break things so we remove them
		
		@SuppressWarnings("unchecked")
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
		ScaledResolution r = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		int f = r.getScaleFactor();
		
		GL11.glScissor(x * f, (r.getScaledHeight() - y - h)*f, w * f, h * f);
	}
	
	/**
	 * Similar to normally splitting a string with the fontRenderer however this variant preserves
	 * the original characters (including new line) and does not not attempt to preserver the format
	 * between lines.
	 */
	public static List<String> splitStringWithoutFormat(String str, int wrapWidth, FontRenderer font)
	{
		List<String> list = new ArrayList<String>();
		
		String lastFormat = ""; // Formatting like bold can affect the wrapping width
		
		String[] nlSplit = str.split("\n");
		
		for(int i = 0; i < nlSplit.length; i++)
		{
			String s = nlSplit[i] + (i + 1 < nlSplit.length? "\n" : ""); // Preserve new line characters for indexing accuracy
			
			while(font.getStringWidth(s) >= wrapWidth)
			{
				lastFormat = getFormatFromString(lastFormat + s);
				int n = sizeStringToWidth(lastFormat + s, wrapWidth, font);
				n -= lastFormat.length();
				n = Math.max(1, n);
				String subTxt = s.substring(0, n);
				list.add(subTxt);
				s = s.replaceFirst(subTxt, "");
			}
			
			list.add(s);
		}
        
        return list;
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

                        if (c1 != 108 && c1 != 76)
                        {
                            if (c1 == 114 || c1 == 82 || isFormatColor(c1))
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

            if (c0 == 10)
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
        return colorChar >= 48 && colorChar <= 57 || colorChar >= 97 && colorChar <= 102 || colorChar >= 65 && colorChar <= 70;
    }
    
    private static boolean isFormatSpecial(char p_78270_0_)
    {
        return p_78270_0_ >= 107 && p_78270_0_ <= 111 || p_78270_0_ >= 75 && p_78270_0_ <= 79 || p_78270_0_ == 114 || p_78270_0_ == 82;
    }
}
