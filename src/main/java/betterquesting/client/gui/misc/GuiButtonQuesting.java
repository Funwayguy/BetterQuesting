package betterquesting.client.gui.misc;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import betterquesting.client.themes.ThemeRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonQuesting extends GuiButton
{
	ArrayList<String> toolTip = new ArrayList<String>();
	boolean txtShadow = true;
	ResourceLocation icon = null;
	boolean is = false;
	int iu = 0;
	int iv = 0;
	int iw = 0;
	int ih = 0;
	
	public GuiButtonQuesting(int id, int x, int y, String text)
	{
		super(id, x, y, text);
	}
	
	public GuiButtonQuesting(int id, int x, int y, int width, int height, String text)
	{
		super(id, x, y, width, height, text);
	}
	
	public void disableShadow()
	{
		txtShadow = false;
	}
	
	/**
	 * Sets icon underneath text. Can be set to scale with the button
	 */
	public GuiButtonQuesting setIcon(ResourceLocation texture, int u, int v, int w, int h, boolean autoScale)
	{
		this.icon = texture;
		this.is = autoScale;
		this.iu = u;
		this.iv = v;
		this.iw = w;
		this.ih = h;
		return this;
	}
	
	public void setTooltip(ArrayList<String> toolTip)
	{
		this.toolTip = toolTip;
	}
	
	public ArrayList<String> getTooltip()
	{
		return toolTip;
	}

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft mc, int mx, int my)
    {
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(ThemeRegistry.curTheme().guiTexture());
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_146123_n = mx >= this.xPosition && my >= this.yPosition && mx < this.xPosition + this.width && my < this.yPosition + this.height;
            int k = this.getHoverState(this.field_146123_n);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
            GL11.glPushMatrix();
            float sh = height/20F;
            float sw = width >= 196? width/200F : 1F;
            float py = yPosition/sh;
            float px = xPosition/sw;
            GL11.glScalef(sw, sh, 1F);
            
            if(width > 200) // Could use 396 but limiting it to 200 this makes things look nicer
            {
                GL11.glTranslatef(px, py, 0F); // Fixes floating point errors related to position
            	this.drawTexturedModalRect(0, 0, 48, k * 20, 200, 20);
            } else
            {
            	this.drawTexturedModalRect((int)px, (int)py, 48, k * 20, this.width / 2, 20);
            	this.drawTexturedModalRect((int)px + width / 2, (int)py, 248 - this.width / 2, k * 20, this.width / 2, 20);
            }
            
            GL11.glPopMatrix();
            
            this.mouseDragged(mc, mx, my);
            int l = 14737632;

            if (packedFGColour != 0)
            {
                l = packedFGColour;
            }
            else if (!this.enabled)
            {
                l = 10526880;
            }
            else if (this.field_146123_n)
            {
                l = 16777120;
            }
            
            if(icon != null)
            {
            	mc.renderEngine.bindTexture(icon);
            	if(is)
            	{
            		float iScale = Math.min(width/(float)iw, height/(float)ih);
            		GL11.glPushMatrix();
            		GL11.glScalef(iScale, iScale, 1F);
            		GL11.glTranslatef(xPosition/iScale, yPosition/iScale, 0F);
            		this.drawTexturedModalRect((int)(width/2 - (iw*iScale)/2F), (int)(height/2 - (ih*iScale)/2F), iu, iv, iw, ih);
            		GL11.glPopMatrix();
            	} else
            	{
            		this.drawTexturedModalRect(xPosition + width/2 - iw/2, yPosition + height/2 - ih/2, iu, iv, iw, ih);
            	}
            }
            
            String txt = this.displayString;
            
            if(fontrenderer.getStringWidth(txt) > width) // Auto crop text to keep things tidy
            {
            	int dotWidth = fontrenderer.getStringWidth("...");
            	txt = fontrenderer.trimStringToWidth(txt, width - dotWidth) + "...";
            }
            
            this.drawCenteredString(fontrenderer, txt, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l, txtShadow);
        }
    }
	
	@Override
	public void func_146113_a(SoundHandler p_146113_1_)
    {
        p_146113_1_.playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
    }
	
    /**
     * Renders the specified text to the screen, center-aligned. Has additional shadow option
     */
    public void drawCenteredString(FontRenderer fontrender, String s, int x, int y, int color, boolean shadow)
    {
        fontrender.drawString(s, x - fontrender.getStringWidth(s) / 2, y, color, shadow);
    }
}
