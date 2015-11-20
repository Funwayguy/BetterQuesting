package betterquesting.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.editors.GuiQuestEditor;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.RenderUtils;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestInstance extends GuiQuesting
{
	QuestInstance quest;
	int selTask = 0;
	int selReward = 0;
	GuiButtonQuesting btnTLeft;
	GuiButtonQuesting btnTRight;
	GuiButtonQuesting btnRLeft;
	GuiButtonQuesting btnRRight;
	GuiButtonQuesting btnClaim;
	
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
		
		this.title = quest.name;
		this.selReward = 0;
		this.selTask = 0;
		
		((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
		((GuiButton)this.buttonList.get(0)).width = 100;
		
		GuiButtonQuesting btnEdit = new GuiButtonQuesting(4, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, "Edit");
		btnEdit.enabled = true;
		this.buttonList.add(btnEdit);
		
		btnTLeft = new GuiButtonQuesting(1, this.guiLeft + (sizeX/4)*3 - 70, this.guiTop + sizeY - 48, 20, 20, "<");
		btnTLeft.enabled = selTask > 0;
		btnTRight = new GuiButtonQuesting(3, this.guiLeft + (sizeX/4)*3 + 50, this.guiTop + sizeY - 48, 20, 20, ">");
		btnTRight.enabled = selTask < quest.tasks.size() - 1;

		btnRLeft = new GuiButtonQuesting(6, this.guiLeft + (sizeX/4) - 70, this.guiTop + sizeY - 48, 20, 20, "<");
		btnRLeft.enabled = selReward > 0;
		btnRRight = new GuiButtonQuesting(7, this.guiLeft + (sizeX/4) + 50, this.guiTop + sizeY - 48, 20, 20, ">");
		btnRRight.enabled = selReward < quest.rewards.size() - 1;
		
		GuiButtonQuesting btnDetect = new GuiButtonQuesting(2, this.guiLeft + (sizeX/4)*3 - 50, this.guiTop + sizeY - 48, 100, 20, "Detect/Submit");
		btnDetect.enabled = !quest.isComplete(mc.thePlayer.getUniqueID());
		btnClaim = new GuiButtonQuesting(5, this.guiLeft + (sizeX/4) - 50, this.guiTop + sizeY - 48, 100, 20, "Claim Rewards");
		btnClaim.enabled = quest.CanClaim(mc.thePlayer, quest.GetChoiceData());
		this.buttonList.add(btnTLeft);
		this.buttonList.add(btnTRight);
		this.buttonList.add(btnRLeft);
		this.buttonList.add(btnRRight);
		this.buttonList.add(btnDetect);
		this.buttonList.add(btnClaim);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(QuestDatabase.updateUI)
		{
			QuestInstance nq = QuestDatabase.getQuestByID(quest.questID);
			
			if(nq == null)
			{
				mc.displayGuiScreen(parent);
				return;
			} else
			{
				quest = nq;
				this.initGui();
				QuestDatabase.updateUI = false;
			}
		}
		
		this.fontRendererObj.drawSplitString(quest.description, this.guiLeft + 16, this.guiTop + 32, sizeX/2 - 16, ThemeRegistry.curTheme().textColor().getRGB());
		
		RenderUtils.DrawLine(this.guiLeft + sizeX/2, this.guiTop + 32, this.guiLeft + sizeX/2, this.guiTop + sizeY - 28, 1, ThemeRegistry.curTheme().textColor());
		
		TaskBase task = selTask < quest.tasks.size()? quest.tasks.get(selTask) : null;
		
		if(task != null)
		{
			String tTitle = task.getDisplayName();
			
			if(quest.tasks.size() > 1)
			{
				tTitle = (selTask + 1) + "/" + quest.tasks.size() + " " + tTitle;
			}
			
			tTitle = ChatFormatting.UNDERLINE + tTitle;
			
			int nameWidth = this.fontRendererObj.getStringWidth(tTitle);
			this.fontRendererObj.drawString(tTitle, this.guiLeft + (sizeX/4)*3 - (nameWidth/2), this.guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB());
			GL11.glPushMatrix(); 
			GL11.glColor4f(1F, 1F, 1F, 1F);
			task.drawQuestInfo(this, mx, my, this.guiLeft + this.sizeX/2 + 8, this.guiTop + 48, sizeX/2 - 24, sizeY - 104);
			GL11.glPopMatrix();
		}
		
		RewardBase reward = selReward < quest.rewards.size()? quest.rewards.get(selReward) : null;
		
		if(reward != null)
		{
			String rTitle = reward.getDisplayName();
			
			if(quest.rewards.size() > 1)
			{
				rTitle = (selReward + 1) + "/" + quest.rewards.size() + " " + rTitle;
			}
			
			rTitle = ChatFormatting.UNDERLINE + rTitle;
			
			int nameWidth = this.fontRendererObj.getStringWidth(rTitle);
			this.fontRendererObj.drawString(rTitle, this.guiLeft + (sizeX/4)*1 - (nameWidth/2), this.guiTop + sizeY/2, ThemeRegistry.curTheme().textColor().getRGB());
			GL11.glPushMatrix();
			GL11.glColor4f(1F, 1F, 1F, 1F);
			reward.drawReward(this, mx, my, this.guiLeft + 16, this.guiTop + sizeY/2 + 12, sizeX/2 - 8, sizeY - 104);
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
			btnTLeft.enabled = selTask > 0;
			btnTRight.enabled = selTask < quest.tasks.size() - 1;
		} else if(btn.id == 2) // Manual detect
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", 3);
			tags.setInteger("questID", quest.questID);
			BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
		} else if(btn.id == 3) // Task right
		{
			selTask++;
			btnTLeft.enabled = selTask > 0;
			btnTRight.enabled = selTask < quest.tasks.size() - 1;
		} else if(btn.id == 4) // Edit Quest
		{
			mc.displayGuiScreen(new GuiQuestEditor(this, quest));
		} else if(btn.id == 5) // Claim reward
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", 4);
			tags.setInteger("questID", quest.questID);
			tags.setTag("ChoiceData", quest.GetChoiceData());
			BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
		} else if(btn.id == 6)
		{
			selReward--;
			btnRLeft.enabled = selReward > 0;
			btnRRight.enabled = selReward < quest.rewards.size() - 1;
		} else if(btn.id == 7)
		{
			selReward++;
			btnRLeft.enabled = selReward > 0;
			btnRRight.enabled = selReward < quest.rewards.size() - 1;
		}
	}
	
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click)
    {
    	super.mouseClicked(mx, my, click);
    	
    	RewardBase reward = selReward < quest.rewards.size()? quest.rewards.get(selReward) : null;
		
		if(reward != null)
		{
			reward.MousePressed(this, mx, my, this.guiLeft + 16, this.guiTop + sizeY/2 + 8, sizeX/2 - 8, sizeY - 104, click);
			btnClaim.enabled = quest.CanClaim(mc.thePlayer, quest.GetChoiceData());
		}
		
		TaskBase task = selTask < quest.tasks.size()? quest.tasks.get(selTask) : null;
		
		if(task != null)
		{
			task.MousePressed(this, mx, my, this.guiLeft + this.sizeX/2 + 8, this.guiTop + 48, sizeX/2 - 24, sizeY - 104, click);
		}
    }
}
