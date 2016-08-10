package betterquesting.api.client.gui.premade.controls;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.utils.RenderUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonQuestInstance extends GuiButtonThemed
{
	private IQuestContainer quest;
	private ArrayList<GuiButtonQuestInstance> parents = new ArrayList<GuiButtonQuestInstance>();
	
	public GuiButtonQuestInstance(int id, int x, int y, int w, int h, IQuestContainer quest)
	{
		super(id, x, y, w, h, "", false);
		this.quest = quest;
	}
	
	public void addParent(GuiButtonQuestInstance btn)
	{
		parents.add(btn);
	}

    /**
     * Draws this button to the screen.
     */
	@Override
    public void drawButton(Minecraft mc, int mx, int my)
    {
		if(ExpansionAPI.INSTANCE.getProperties().isEditMode())
		{
			this.enabled = this.visible = true;
		} else if(mc.thePlayer == null)
		{
			this.enabled = false;
			this.visible = true;
		} else
		{
			this.visible = isQuestShown(mc.thePlayer.getUniqueID());
			this.enabled = this.visible && quest.isUnlocked(mc.thePlayer.getUniqueID());
		}
		
        if (this.visible)
        {
            mc.getTextureManager().bindTexture(currentTheme().getGuiTexture());
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_146123_n = this.mousePressed(mc, mx, my);
            int state = this.getHoverState(this.field_146123_n);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
        	EnumQuestState questState = quest.getState(mc.thePlayer.getUniqueID());
        	
        	for(GuiButtonQuestInstance p : parents)
        	{
        		if(!p.visible)
        		{
        			continue;
        		}
        		
        		float lsx = p.xPosition + p.width/2F;
        		float lsy = p.yPosition + p.height/2F;
        		float lex = xPosition + width/2F;
        		float ley = yPosition + height/2F;
        		
        		double la = Math.atan2(ley - lsy, lex - lsx);
        		double dx = Math.cos(la) * 16;
        		double dy = Math.sin(la) * 16;
        		lsx += MathHelper.clamp_float((float)dx, -12, 12);
        		lsy += MathHelper.clamp_float((float)dy, -12, 12);
        		
        		la = Math.atan2(lsy - ley, lsx - lex);
        		dx = Math.cos(la) * 16;
        		dy = Math.sin(la) * 16;
        		lex += MathHelper.clamp_float((float)dx, -12, 12);
        		ley += MathHelper.clamp_float((float)dy, -12, 12);
        		
        		GL11.glPushMatrix();
        		
        		GL11.glDisable(GL11.GL_TEXTURE_2D);
        		
            	int cl = currentTheme().getQuestLineColor(quest, questState);
        		float lr = (float)(cl >> 16 & 255) / 255.0F;
                float lg = (float)(cl >> 8 & 255) / 255.0F;
                float lb = (float)(cl & 255) / 255.0F;
            	GL11.glColor4f(lr, lg, lb, 1F);
        		
        		GL11.glLineWidth(currentTheme().getLineWidth(quest, questState));
        		GL11.glBegin(GL11.GL_LINES);
        		GL11.glLineStipple(1, currentTheme().getLineStipple(quest, questState));
        		
        		GL11.glVertex2f(lsx, lsy);
        		GL11.glVertex2f(lex, ley);
        		GL11.glEnd();
        		
        		GL11.glEnable(GL11.GL_TEXTURE_2D);
        		GL11.glColor4f(1F, 1F, 1F, 1F);
        		
        		GL11.glPopMatrix();
        	}
            
        	int ci = currentTheme().getQuestIconColor(quest, questState, state);
    		float ir = (float)(ci >> 16 & 255) / 255.0F;
            float ig = (float)(ci >> 8 & 255) / 255.0F;
            float ib = (float)(ci & 255) / 255.0F;
        	GL11.glColor4f(ir, ig, ib, 1F);
        	
        	GL11.glPushMatrix();
        	GL11.glTranslatef(this.xPosition, this.yPosition, 0F);
        	float sw = width / 24;
        	float sh = height / 24;
        	GL11.glScalef(sw, sh, 0F);
        	this.drawTexturedModalRect(0, 0, (quest.isMain()? 24 : 0), 104, 24, 24);
        	
        	if(quest.getIcon() != null)
        	{
        		RenderUtils.RenderItemStack(mc, quest.getIcon().getBaseStack(), 4, 4, "");
        	}
        	
        	GL11.glPopMatrix();
        	
        	this.mouseDragged(mc, mx, my);
        }
    }
	
	public boolean isQuestShown(UUID uuid)
	{
		if(ExpansionAPI.INSTANCE.getProperties().isEditMode() || quest.getVisibility() == EnumQuestVisibility.ALWAYS)
		{
			return true;
		} else if(quest.getVisibility() == EnumQuestVisibility.HIDDEN)
		{
			return false;
		} else if(quest.getVisibility() == EnumQuestVisibility.UNLOCKED)
		{
			return quest.isUnlocked(uuid) || quest.isComplete(uuid);
		} else if(quest.getVisibility() == EnumQuestVisibility.NORMAL)
		{
			if(!quest.isComplete(uuid))
			{
				for(GuiButtonQuestInstance p : parents)
				{
					if(!p.quest.isUnlocked(uuid))
					{
						return false; // We require something locked
					}
				}
			}
			
			return true;
		} else if(quest.getVisibility() == EnumQuestVisibility.COMPLETED)
		{
			return quest.isComplete(uuid);
		} else if(quest.getVisibility() == EnumQuestVisibility.CHAIN)
		{
			for(GuiButtonQuestInstance q : parents)
			{
				if(q.isQuestShown(uuid))
				{
					return true;
				}
			}
			
			return parents.size() <= 0;
		}
		
		return true;
	}
}
