package betterquesting.utils;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import betterquesting.client.gui.GuiQuesting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderUtils
{
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
        float a = (float)(color >> 24 & 255) / 255.0F;
		RenderItemStack(mc, stack, x, y, text, new Color(r, g, b, a));
	}
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, Color color)
	{
		GL11.glPushMatrix();
        
		try
		{
		    GL11.glColor4b((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue(), (byte)color.getAlpha());
			RenderHelper.enableGUIStandardItemLighting();
		    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			
		    GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		    itemRender.zLevel = 200.0F;
		    FontRenderer font = null;
		    if (stack != null) font = stack.getItem().getFontRenderer(stack);
		    if (font == null) font = mc.fontRenderer;
		    itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), stack, x, y);
		    itemRender.renderItemOverlayIntoGUI(font, mc.getTextureManager(), stack, x, y, text);
		    itemRender.zLevel = 0.0F;
		    
		    GL11.glDisable(GL11.GL_LIGHTING);
		} catch(Exception e)
		{
		}
		
        GL11.glPopMatrix();
	}

    public static void RenderEntity(int posX, int posY, int scale, float rotation, float pitch, Entity entity)
    {
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
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
    }
	
	public static void DrawLine(int x1, int y1, int x2, int y2, float width, Color color)
	{
		GL11.glPushMatrix();
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F, color.getAlpha()/255F);
		GL11.glLineWidth(width);
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(x1, y1);
		GL11.glVertex2f(x2, y2);
		GL11.glEnd();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		GL11.glPopMatrix();
	}
	
	public static void DrawFakeButton(GuiQuesting screen, int x, int y, int width, int height, String text, int state)
	{
        FontRenderer fontrenderer = screen.mc.fontRenderer;
        screen.mc.getTextureManager().bindTexture(new ResourceLocation("betterquesting", "textures/gui/editor_gui.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
        GL11.glColor4f(1F, 1F, 1F, 1F);
	}
}
