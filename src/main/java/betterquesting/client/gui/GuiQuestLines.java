package betterquesting.client.gui;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.editors.GuiQuestLineEditorA;
import betterquesting.client.gui.misc.GuiButtonQuestInstance;
import betterquesting.client.gui.misc.GuiButtonQuestLine;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.RenderUtils;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestLines extends GuiQuesting
{
	GuiButtonQuestLine selected;
	ArrayList<GuiButtonQuestLine> qlBtns = new ArrayList<GuiButtonQuestLine>();
	int listScroll = 0;
	int maxRows = 0;
	int boxScrollX = 0;
	int boxScrollY = 0;
	int maxScrollX = 0;
	int maxScrollY = 0;
	
	public GuiQuestLines(GuiScreen parent)
	{
		super(parent, "Quest Lines");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		qlBtns.clear();
		
		listScroll = 0;
		maxRows = (sizeY - 64)/20;
		
		((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
		((GuiButton)this.buttonList.get(0)).width = 100;
		
		GuiButtonQuesting btnEdit = new GuiButtonQuesting(1, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, "Edit");
		btnEdit.enabled = true;
		this.buttonList.add(btnEdit);
		
		boolean reset = true;
		
		int i = 0;
		for(QuestLine line : QuestDatabase.questLines)
		{
			GuiButtonQuestLine btnLine = new GuiButtonQuestLine(buttonList.size(), this.guiLeft + 16, this.guiTop + 32 + i, 142, 20, line);
			btnLine.enabled = line.questList.size() <= 0;
			
			if(selected != null && selected.line == line)
			{
				reset = false;
				selected = btnLine;
			}
			
			for(GuiButtonQuestInstance btnQuest : btnLine.buttonTree)
			{
				btnQuest.SetClampingBounds(this.guiLeft + 174, this.guiTop + 32, this.sizeX - (32 + 150 + 8), this.sizeY - 64 - 32);
				btnQuest.xPosition += this.guiLeft + 174 + (this.sizeX - (32 + 150 + 8))/2 - btnLine.treeW/2;
				btnQuest.yPosition += this.guiTop + 32 + (this.sizeY - 64 - 32)/2 - btnLine.treeH/2;
				
				for(GuiButtonQuestInstance p : btnLine.buttonTree)
				{
					if(p.quest.isUnlocked(mc.thePlayer.getUniqueID()) && (selected == null || selected.line != line))
					{
						btnLine.enabled = true;
					}
					
					if(btnQuest.quest.preRequisites.contains(p.quest))
					{
						btnQuest.parent = p;
						break;
					}
				}
			}
			buttonList.add(btnLine);
			qlBtns.add(btnLine);
			i += 20;
		}
		
		if(reset || selected == null)
		{
			selected = null;
		} else
		{
			maxScrollX = Math.abs((this.sizeX - (32 + 150 + 8))/2 - (selected.treeW + 32)/2);
			maxScrollY = Math.abs((this.sizeY - 64 - 32)/2 - (selected.treeH + 32)/2);
			boxScrollX = 0;
			boxScrollY = maxScrollY;
		}
		
		UpdateScroll();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(QuestDatabase.updateUI)
		{
			QuestDatabase.updateUI = false;
			this.initGui();
		}
		
		this.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		GL11.glPushMatrix();
		int mapSX = this.sizeX - (32 + 150 + 8);
		int mapSY = this.sizeY - 64 - 32;
		double scaleX = mapSX/128D;
		double scaleY = mapSY/128D;
		GL11.glScaled(scaleX, scaleY, 1F);
		this.drawTexturedModalRect((int)Math.round((this.guiLeft + 174)/scaleX), (int)Math.round((this.guiTop + 32)/scaleY), 0, 128, 128, 128);
		GL11.glPopMatrix();
		
		this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32, 248, 0, 8, 20);
		int i = 20;
		while(i < sizeY - 84)
		{
			this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32 + i, 248, 20, 8, 20);
			i += 20;
		}
		this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32 + i, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + 16 + 142, this.guiTop + 32 + (int)Math.max(0, i * (float)listScroll/(float)(qlBtns.size() - maxRows)), 248, 60, 8, 20);
		
		QuestInstance qTooltip = null;
		
		if(selected != null)
		{
			for(GuiButtonQuestInstance btnQuest : selected.buttonTree)
			{
				btnQuest.SetScrollOffset(boxScrollX, boxScrollY);
				btnQuest.drawButton(mc, mx, my);
				
				if(btnQuest.visible && this.isWithin(mx, my, btnQuest.xPosition + boxScrollX, btnQuest.yPosition + boxScrollY, btnQuest.width, btnQuest.height, false))
				{
					qTooltip = btnQuest.quest;
				}
			}
			
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			RenderUtils.drawSplitString(fontRendererObj, selected.line.description, this.guiLeft + 174, this.guiTop + 32 + this.sizeY - 64 - 32 + 4, this.sizeX - (32 + 150 + 8), ThemeRegistry.curTheme().textColor().getRGB(), false);
			
			GL11.glPushMatrix();
			float scale = sizeX > 600? 1.5F : 1F;
			GL11.glScalef(scale, scale, scale);
			mc.fontRenderer.drawString(ChatFormatting.BOLD + selected.line.name, MathHelper.ceiling_float_int((this.guiLeft + 180)/scale), MathHelper.ceiling_float_int((this.guiTop + 38)/scale), ThemeRegistry.curTheme().textColor().getRGB(), false);
			GL11.glPopMatrix();
		}
		
		if(qTooltip != null)
		{
			ArrayList<String> qInfo = new ArrayList<String>();
			qInfo.add(qTooltip.name);
			if(qTooltip.isComplete(mc.thePlayer.getUniqueID()))
			{
				qInfo.add(ChatFormatting.GREEN + "COMPLETE");
				
				if(!qTooltip.HasClaimed(mc.thePlayer.getUniqueID()))
				{
					qInfo.add(ChatFormatting.GRAY + "Rewards pending...");
				}
			} else if(!qTooltip.isUnlocked(mc.thePlayer.getUniqueID()))
			{
				qInfo.add(ChatFormatting.RED + "" + ChatFormatting.UNDERLINE + "REQUIRES:");
				
				for(QuestInstance req : qTooltip.preRequisites)
				{
					if(!req.isComplete(mc.thePlayer.getUniqueID()))
					{
						qInfo.add(ChatFormatting.RED + "- " + req.name);
					}
				}
			} else
			{
				int n = 0;
				
				for(TaskBase task : qTooltip.tasks)
				{
					if(task.isComplete(mc.thePlayer))
					{
						n++;
					}
				}
				
				qInfo.add(ChatFormatting.GRAY + "" + n + "/" + qTooltip.tasks.size() + " Tasks complete");
			}
			this.drawHoveringText(qInfo, mx, my, this.fontRendererObj);
		}
	}
	
	boolean flag = false;
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		flag = true;
		
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			mc.displayGuiScreen(new GuiQuestLineEditorA(this));
			// Quest line editor
		} else if(button instanceof GuiButtonQuestLine)
		{
			if(selected != null)
			{
				selected.enabled = true;
			}
			
			button.enabled = false;
			
			selected = (GuiButtonQuestLine)button;
			maxScrollX = Math.abs((this.sizeX - (32 + 150 + 8))/2 - (selected.treeW + 32)/2);
			maxScrollY = Math.abs((this.sizeY - 64 - 32)/2 - (selected.treeH + 32)/2);
			boxScrollX = 0;
			boxScrollY = maxScrollY;
		}
	}
	
	@Override
    protected void mouseClicked(int mx, int my, int type)
    {
		flag = false;
		
		super.mouseClicked(mx, my, type);
		
		if(!flag && selected != null)
		{
			for(GuiButtonQuestInstance btnQuest : selected.buttonTree)
			{
				if(btnQuest.mousePressed(mc, mx, my))
				{
					flag = true;
					btnQuest.func_146113_a(this.mc.getSoundHandler());
					mc.displayGuiScreen(new GuiQuestInstance(this, btnQuest.quest));
					break;
				}
			}
		}
    }
	
	@Override
	public void handleMouseInput()
    {
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		listScroll = Math.max(0, MathHelper.clamp_int(listScroll + SDX, 0, qlBtns.size() - maxRows));
    		UpdateScroll();
        }
        
    	if(!flag && Mouse.isButtonDown(0))
    	{
    		this.boxScrollX += Mouse.getEventDX() * this.width / this.mc.displayWidth;
    		this.boxScrollY -= Mouse.getEventDY() * this.height / this.mc.displayHeight;
    		this.boxScrollX = MathHelper.clamp_int(boxScrollX, -maxScrollX, maxScrollX);
    		this.boxScrollY = MathHelper.clamp_int(boxScrollY, -maxScrollY, maxScrollY);
    	}
    }
	
	public void UpdateScroll()
	{
		// All buttons are required to be present due to the button trees
		// These are hidden and moved as necessary
		for(int i = 0; i < qlBtns.size(); i++)
		{
			GuiButtonQuestLine btn = qlBtns.get(i);
			int n = i - listScroll;
			
			if(n < 0 || n >= maxRows)
			{
				btn.visible = false;
			} else
			{
				btn.visible = true;
				btn.yPosition = this.guiTop + 32 + n*20;
			}
		}
	}
}
