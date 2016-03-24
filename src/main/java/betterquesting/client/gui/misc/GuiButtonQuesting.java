package betterquesting.client.gui.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import betterquesting.client.themes.ThemeRegistry;

@SideOnly(Side.CLIENT)
public class GuiButtonQuesting extends GuiButton
{
	public GuiButtonQuesting(int id, int x, int y, String text)
	{
		super(id, x, y, text);
	}
	
	public GuiButtonQuesting(int id, int x, int y, int width, int height, String text)
	{
		super(id, x, y, width, height, text);
	}

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_)
    {
        if (this.visible)
        {
            FontRenderer fontrenderer = p_146112_1_.fontRendererObj;
            p_146112_1_.getTextureManager().bindTexture(ThemeRegistry.curTheme().guiTexture());
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = p_146112_2_ >= this.xPosition && p_146112_3_ >= this.yPosition && p_146112_2_ < this.xPosition + this.width && p_146112_3_ < this.yPosition + this.height;
            int k = this.getHoverState(this.hovered);
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
            
            this.mouseDragged(p_146112_1_, p_146112_2_, p_146112_3_);
            int l = 14737632;

            if (packedFGColour != 0)
            {
                l = packedFGColour;
            }
            else if (!this.enabled)
            {
                l = 10526880;
            }
            else if (this.hovered)
            {
                l = 16777120;
            }
            
            String txt = this.displayString;
            
            if(fontrenderer.getStringWidth(txt) > width) // Auto crop text to keep things tidy
            {
            	int dotWidth = fontrenderer.getStringWidth("...");
            	txt = fontrenderer.trimStringToWidth(txt, width - dotWidth) + "...";
            }
            
            this.drawCenteredString(fontrenderer, txt, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
        }
    }
}
