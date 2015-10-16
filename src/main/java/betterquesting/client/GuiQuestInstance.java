package betterquesting.client;

import java.awt.Color;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.opengl.GL11;
import com.google.gson.JsonObject;
import betterquesting.client.buttons.GuiButtonQuesting;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.RenderUtils;

public class GuiQuestInstance extends GuiQuesting
{
	JsonObject lastEdit;
	QuestInstance quest;
	String[] wrappedDesc = new String[]{};
	int selTask = 0;
	int selReward = 0;
	GuiButtonQuesting btnLeft;
	GuiButtonQuesting btnRight;
	
	public GuiQuestInstance(GuiScreen parent, QuestInstance quest)
	{
		super(parent, quest.name);
		this.quest = quest;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		if(lastEdit != null)
		{
			quest.readFromJSON(lastEdit);
			lastEdit = null;
		}
		
		this.title = quest.name;
		wrappedDesc = RenderUtils.WordWrap(fontRendererObj, quest.description, sizeX/2 - 16);
		
		((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
		((GuiButton)this.buttonList.get(0)).width = 100;
		
		GuiButtonQuesting btnEdit = new GuiButtonQuesting(4, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, "Edit");
		btnEdit.enabled = true;
		this.buttonList.add(btnEdit);
		
		btnLeft = new GuiButtonQuesting(1, this.guiLeft + (sizeX/4)*3 - 70, this.guiTop + sizeY - 48, 20, 20, "<");
		btnLeft.enabled = selTask > 0;
		btnRight = new GuiButtonQuesting(3, this.guiLeft + (sizeX/4)*3 + 50, this.guiTop + sizeY - 48, 20, 20, ">");
		btnRight.enabled = selTask < quest.questTypes.size() - 1;
		GuiButtonQuesting btnDetect = new GuiButtonQuesting(2, this.guiLeft + (sizeX/4)*3 - 50, this.guiTop + sizeY - 48, 100, 20, "Detect/Submit");
		btnDetect.enabled = !quest.isComplete(mc.thePlayer.getUniqueID());
		this.buttonList.add(btnLeft);
		this.buttonList.add(btnRight);
		this.buttonList.add(btnDetect);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		for(int i = 0; i < wrappedDesc.length; i++)
		{
			this.fontRendererObj.drawString(wrappedDesc[i], this.guiLeft + 16, this.guiTop + 32 + (i * 10), Color.BLACK.getRGB(), false);
		}
		
		RenderUtils.DrawLine(this.guiLeft + sizeX/2, this.guiTop + 32, this.guiLeft + sizeX/2, this.guiTop + sizeY - 28, 1, Color.BLACK);
		
		TaskBase task = selTask < quest.getQuests().size()? quest.getQuests().get(selTask) : null;
		
		if(task != null)
		{
			String tTitle = task.getClass().getSimpleName();
			
			if(quest.questTypes.size() > 1)
			{
				tTitle = (selTask + 1) + "/" + quest.questTypes.size() + " " + tTitle;
			}
			
			int nameWidth = this.fontRendererObj.getStringWidth(tTitle);
			this.fontRendererObj.drawString(tTitle, this.guiLeft + (sizeX/4)*3 - (nameWidth/2), this.guiTop + 32, Color.BLACK.getRGB());
			GL11.glPushMatrix();
			GL11.glColor4f(1F, 1F, 1F, 1F);
			task.drawQuestInfo(this, mx, my, this.guiLeft + this.sizeX/2 + 8, this.guiTop + 48, sizeX/2 - 24, sizeY - 104);
			GL11.glPopMatrix();
		}
		
		RewardBase reward = selReward < quest.rewards.size()? quest.rewards.get(selReward) : null;
		
		if(reward != null)
		{
			String tTitle = reward.getClass().getSimpleName();
			
			if(quest.questTypes.size() > 1)
			{
				tTitle = (selReward + 1) + "/" + quest.rewards.size() + " " + tTitle;
			}
			
			int nameWidth = this.fontRendererObj.getStringWidth(tTitle);
			this.fontRendererObj.drawString(tTitle, this.guiLeft + (sizeX/4)*1 - (nameWidth/2), this.guiTop + sizeY/2, Color.BLACK.getRGB());
			GL11.glPushMatrix();
			GL11.glColor4f(1F, 1F, 1F, 1F);
			/*GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glBegin(GL11.GL_QUADS);
		    GL11.glVertex2f(this.guiLeft + 16, this.guiTop + sizeY/2 + 8);
		    GL11.glVertex2f(this.guiLeft + sizeX/2 - 8, this.guiTop + sizeY/2 + 8);
		    GL11.glVertex2f(this.guiLeft + sizeX/2 - 8, this.guiTop + sizeY - 104);
		    GL11.glVertex2f(this.guiLeft + 16, this.guiTop + sizeY - 104);
			GL11.glEnd();*/
			//GuiScreen.drawRect(this.guiLeft + 16, this.guiTop + sizeY/2 + 8, this.guiLeft + sizeX/2 - 8, this.guiTop + sizeY - 104, Color.BLACK.getRGB());
			reward.drawReward(this, mx, my, this.guiLeft + 16, this.guiTop + sizeY/2 + 8, sizeX/2 - 8, sizeY - 104);
			GL11.glPopMatrix();
		}
	}
	
	@Override
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id == 1) // Task left
		{
			selTask--;
			btnLeft.enabled = selTask > 0;
			btnRight.enabled = selTask < quest.questTypes.size() - 1;
		} else if(btn.id == 2) // Manual detect
		{
			TaskBase task = selTask < quest.getQuests().size()? quest.getQuests().get(selTask) : null;
			
			if(task != null)
			{
				task.Detect(mc.thePlayer);
			}
		} else if(btn.id == 3) // Task right
		{
			selTask++;
			btnLeft.enabled = selTask > 0;
			btnRight.enabled = selTask < quest.questTypes.size() - 1;
		} else if(btn.id == 4)
		{
			this.lastEdit = new JsonObject();
			quest.writeToJSON(lastEdit);
			mc.displayGuiScreen(new GuiJsonObject(this, lastEdit));
		}
	}
}
