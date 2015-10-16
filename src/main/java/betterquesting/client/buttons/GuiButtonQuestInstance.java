package betterquesting.client.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import betterquesting.quests.QuestInstance;

public class GuiButtonQuestInstance extends GuiButtonQuesting
{
	public QuestInstance quest;
	public GuiButtonQuestInstance parent;
	
	// Clamping bounds
	boolean enableClamp = false;
	int clampMinX = 0;
	int clampMaxX = 256;
	int clampMinY = 0;
	int clampMaxY = 256;
	
	// Scrolling offsets
	int offX = 0;
	int offY = 0;
	
	public GuiButtonQuestInstance(int id, int x, int y, QuestInstance quest)
	{
		this(id, x, y, 200, 20, quest);
	}
	
	public GuiButtonQuestInstance(int id, int x, int y, int width, int height, QuestInstance quest)
	{
		super(id, x, y, width, height, quest.name);
		this.quest = quest;
		
		if(Minecraft.getMinecraft().thePlayer == null)
		{
			this.enabled = false;
			this.visible = true;
		} else
		{
			if(parent != null)
			{
				this.visible = parent.quest.isUnlocked(Minecraft.getMinecraft().thePlayer.getUniqueID());
			}
			this.enabled = this.visible && quest.isUnlocked(Minecraft.getMinecraft().thePlayer.getUniqueID());
		}
	}

    /**
     * Draws this button to the screen.
     */
	@Override
    public void drawButton(Minecraft mc, int mx, int my)
    {
        if (this.visible)
        {
        	if(!enableClamp)
        	{
        		this.clampMinX = 0;
        		this.clampMinY = 0;
        		this.clampMaxX = mc.displayWidth;
        		this.clampMaxY = mc.displayHeight;
        	}
        	
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(new ResourceLocation("betterquesting", "textures/gui/editor_gui.png"));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_146123_n = this.mousePressed(mc, mx, my);
            int k = this.getHoverState(this.field_146123_n);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
            int cx = MathHelper.clamp_int(xPosition + offX, clampMinX, clampMaxX);
            int cy = MathHelper.clamp_int(yPosition + offY, clampMinY, clampMaxY);
            int cv = cy - (yPosition + offY) + 20*k;
            int cw = MathHelper.clamp_int(xPosition + offX + width, clampMinX, clampMaxX) - cx;
            int ch = MathHelper.clamp_int(yPosition + offY + height, clampMinY, clampMaxY) - cy;
        	
        	if(parent != null)
        	{
        		float lsx = MathHelper.clamp_float(parent.offX + parent.xPosition + parent.width, clampMinX, clampMaxX);
        		float lsy = MathHelper.clamp_float(parent.offY + parent.yPosition + (parent.height/2F), clampMinY, clampMaxY);
        		float lex = MathHelper.clamp_float(offX + xPosition, clampMinX, clampMaxX);
        		float ley = MathHelper.clamp_float(offY + yPosition + (height/2F), clampMinY, clampMaxY);
        		
        		if(lsx != lex && !(lsy == ley && (lsy == clampMaxY || lsy == clampMinY)))
        		{
	        		GL11.glPushMatrix();
	        		
	        		GL11.glDisable(GL11.GL_TEXTURE_2D);
	        		
	        		if(!enabled)
	        		{
	        			GL11.glColor4f(1F, 0F, 0F, 1F);
	        		} else if(quest.isComplete(mc.thePlayer.getUniqueID()))
	        		{
	        			GL11.glColor4f(0F, 1F, 0F, 1F);
	        		} else
	        		{
	        			GL11.glColor4f(1F, 1F, 0F, 1F);
	        		}
	        		GL11.glLineWidth(4F);
	        		GL11.glBegin(GL11.GL_LINES);
	        		
	        		
	        		GL11.glVertex2f(lsx, lsy);
	        		GL11.glVertex2f(lex, ley);
	        		GL11.glEnd();
	        		
	        		GL11.glEnable(GL11.GL_TEXTURE_2D);
	        		GL11.glColor4f(1F, 1F, 1F, 1F);
	        		
	        		GL11.glPopMatrix();
        		}
        	}
            
            if(cw > 0 && ch > 0)
            {
            	if(cx > clampMinX || cw > width/2)
            	{
                    int cu = cx - (xPosition + offX) + 48;
                    int cw1 = MathHelper.clamp_int(xPosition + offX + width/2, clampMinX, clampMaxX) - cx;
                    
            		this.drawTexturedModalRect(cx, cy, cu, cv, cw1, ch);
            	}
            	
            	if(cx + width/2 < clampMaxX || cw > width/2)
            	{
                    int cx1 = MathHelper.clamp_int(xPosition + offX + width/2, clampMinX, clampMaxX);
                    int cw1 = MathHelper.clamp_int(xPosition + offX + width, clampMinX, clampMaxX) - cx1;
                    int cu = (cx1 - (xPosition + offX + width/2)) + 248 - MathHelper.ceiling_float_int(width/2F);
                    
            		this.drawTexturedModalRect(cx1, cy, cu, cv, cw1, ch);
            	}
            	
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
	            
	            this.drawCenteredString(fontrenderer, fontrenderer.trimStringToWidth(fontrenderer.trimStringToWidth(quest.name, this.width), cw, cx <= clampMinX), cx + cw/2, cy + (ch - 8) / 2, l);
            }
        }
    }
    
    @Override
    public boolean mousePressed(Minecraft mc, int mx, int my)
    {
    	if(!enabled || !visible)
    	{
    		return false;
    	} else if(MathHelper.clamp_int(mx, xPosition + offX, xPosition + width + offX) != mx || MathHelper.clamp_int(my, yPosition + offY, yPosition + height + offY) != my)
    	{
    		return false;
    	} else if(MathHelper.clamp_int(mx, clampMinX, clampMaxX) != mx || MathHelper.clamp_int(my, clampMinY, clampMaxY) != my)
    	{
    		return false;
    	} else
    	{
    		return true;
    	}
    }
	
	/**
	 * Makes this button not render/function outside the given bounds
	 */
	public GuiButtonQuestInstance SetClampingBounds(int posX, int posY, int sizeX, int sizeY)
	{
		this.enableClamp = true;
		this.clampMinX = posX;
		this.clampMinY = posY;
		this.clampMaxX = posX + sizeX;
		this.clampMaxY = posY + sizeY;
		return this;
	}
	
	public GuiButtonQuestInstance SetScrollOffset(int scrollX, int scrollY)
	{
		this.offX = scrollX;
		this.offY = scrollY;
		return this;
	}
}
