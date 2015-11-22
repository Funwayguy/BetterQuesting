package betterquesting.client.gui.misc;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.RenderUtils;

@SideOnly(Side.CLIENT)
public class GuiButtonQuestInstance extends GuiButtonQuesting
{
	Entity itemIcon;
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
		super(id, x, y, 24, 24, "");
		this.quest = quest;
	}

    /**
     * Draws this button to the screen.
     */
	@Override
    public void drawButton(Minecraft mc, int mx, int my)
    {
		if(mc.thePlayer == null)
		{
			this.enabled = false;
			this.visible = true;
		} else
		{
			if(parent != null)
			{
				this.visible = parent.quest.isUnlocked(mc.thePlayer.getUniqueID());
			} else
			{
				this.visible = true;
			}
			this.enabled = this.visible && quest.isUnlocked(mc.thePlayer.getUniqueID());
		}
		
        if (this.visible)
        {
        	if(!enableClamp)
        	{
        		this.clampMinX = 0;
        		this.clampMinY = 0;
        		this.clampMaxX = mc.displayWidth;
        		this.clampMaxY = mc.displayHeight;
        	}
        	
            mc.getTextureManager().bindTexture(new ResourceLocation("betterquesting", "textures/gui/editor_gui.png"));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_146123_n = this.mousePressed(mc, mx, my);
            int state = this.getHoverState(this.field_146123_n);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
            int cx = MathHelper.clamp_int(xPosition + offX, clampMinX, clampMaxX);
            int cy = MathHelper.clamp_int(yPosition + offY, clampMinY, clampMaxY);
            int cw = MathHelper.clamp_int(xPosition + offX + width, clampMinX, clampMaxX) - cx;
            int ch = MathHelper.clamp_int(yPosition + offY + height, clampMinY, clampMaxY) - cy;

        	int questState = !quest.isUnlocked(mc.thePlayer.getUniqueID())? 0 : (!quest.isComplete(mc.thePlayer.getUniqueID())? 1 : (!quest.HasClaimed(mc.thePlayer)? 2 : 3));
        	
        	if(parent != null)
        	{
        		float lsx = MathHelper.clamp_float(parent.offX + parent.xPosition + parent.width/2F, clampMinX, clampMaxX);
        		float lsy = MathHelper.clamp_float(parent.offY + parent.yPosition + parent.height, clampMinY, clampMaxY);
        		float lex = MathHelper.clamp_float(offX + xPosition + width/2F, clampMinX, clampMaxX);
        		float ley = MathHelper.clamp_float(offY + yPosition, clampMinY, clampMaxY);
        		
        		if(!(lsx == lex && (lsx == clampMinX || lex == clampMaxX)) && !(lsy == ley))
        		{
	        		GL11.glPushMatrix();
	        		
	        		GL11.glDisable(GL11.GL_TEXTURE_2D);

	            	Color ci = ThemeRegistry.curTheme().getLineColor(MathHelper.clamp_int(questState, 0, 2), quest.isMain);
	            	GL11.glColor4f(ci.getRed()/255F, ci.getGreen()/255F, ci.getBlue()/255F, 1F);
	        		
	        		GL11.glLineWidth(quest.isMain? 12F : 3F);
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
            	Color ci = ThemeRegistry.curTheme().getIconColor(state, questState, quest.isMain);
            	GL11.glColor4f(ci.getRed()/255F, ci.getGreen()/255F, ci.getBlue()/255F, 1F);
            	
            	this.drawTexturedModalRect(cx, cy, (quest.isMain? 24 : 0) + Math.max(0, cx - (xPosition + offX)), 104 + Math.max(0, cy - (yPosition + offY)), cw, ch);
            	
            	if(itemIcon == null)
            	{
            		EntityItem eItem = new EntityItem(mc.theWorld);
            		eItem.setEntityItemStack(new ItemStack(Items.bed));
            		eItem.hoverStart = 0F;
            		itemIcon = eItem;
            	}
            	
            	if(cw >= width/2 && ch >= height/2 && quest.itemIcon != null)
            	{
            		RenderUtils.RenderItemStack(mc, quest.itemIcon.getBaseStack(), xPosition + offX + 4, yPosition + offY + 4, "");
            	}
            	
            	this.mouseDragged(mc, mx, my);
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
