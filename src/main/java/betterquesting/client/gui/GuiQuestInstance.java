package betterquesting.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.editors.GuiQuestEditor;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.gui.misc.GuiScrollingText;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.RenderUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestInstance extends GuiQuesting
{
	QuestInstance quest;
	int selTask = 0;
	GuiEmbedded taskRender = null;
	int selReward = 0;
	GuiEmbedded rewardRender = null;
	GuiScrollingText desc;
	GuiButtonQuesting btnTLeft;
	GuiButtonQuesting btnTRight;
	GuiButtonQuesting btnRLeft;
	GuiButtonQuesting btnRRight;
	GuiButtonQuesting btnClaim;
	
	/**
	 * Cached between UI updates
	 */
	NBTTagList choiceData = new NBTTagList();
	
	public GuiQuestInstance(GuiScreen parent, QuestInstance quest)
	{
		super(parent, I18n.format(quest.name));
		this.quest = quest;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		this.title = I18n.format(quest.name);
		this.selReward = MathHelper.clamp_int(selReward, 0, Math.max(0, quest.rewards.size() - 1));
		this.taskRender = null;
		this.selTask = MathHelper.clamp_int(selTask, 0, Math.max(0, quest.tasks.size() - 1));
		this.rewardRender = null;
		this.quest.SetChoiceData(choiceData); // Updates choices with any previous values
		
		if(QuestDatabase.editMode)
		{
			((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
			((GuiButton)this.buttonList.get(0)).width = 100;
		}
		
		GuiButtonQuesting btnEdit = new GuiButtonQuesting(4, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, I18n.format("betterquesting.btn.edit"));
		btnEdit.enabled = btnEdit.visible = QuestDatabase.editMode;
		this.buttonList.add(btnEdit);
		
		desc = new GuiScrollingText(this, sizeX/2 - 24, quest.rewards.size() > 0? sizeY/2 - 48 : sizeY - 64, this.guiTop + 32, this.guiLeft + 16, I18n.format(quest.description));
		
		btnTLeft = new GuiButtonQuesting(1, this.guiLeft + (sizeX/4)*3 - 70, this.guiTop + sizeY - 48, 20, 20, "<");
		btnTLeft.enabled = selTask > 0;
		btnTRight = new GuiButtonQuesting(3, this.guiLeft + (sizeX/4)*3 + 50, this.guiTop + sizeY - 48, 20, 20, ">");
		btnTRight.enabled = selTask < quest.tasks.size() - 1;

		btnRLeft = new GuiButtonQuesting(6, this.guiLeft + (sizeX/4) - 70, this.guiTop + sizeY - 48, 20, 20, "<");
		btnRLeft.visible = quest.rewards.size() > 0;
		btnRLeft.enabled = btnRLeft.visible && selReward > 0;
		btnRRight = new GuiButtonQuesting(7, this.guiLeft + (sizeX/4) + 50, this.guiTop + sizeY - 48, 20, 20, ">");
		btnRRight.visible = quest.rewards.size() > 0;
		btnRRight.enabled = btnRRight.visible && selReward < quest.rewards.size() - 1;
		
		GuiButtonQuesting btnDetect = new GuiButtonQuesting(2, this.guiLeft + (sizeX/4)*3 - 50, this.guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.detect_submit"));
		btnDetect.enabled = quest.canSubmit(mc.thePlayer);
		btnClaim = new GuiButtonQuesting(5, this.guiLeft + (sizeX/4) - 50, this.guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.claim"));
		btnClaim.visible = quest.rewards.size() > 0;
		btnClaim.enabled = btnClaim.visible && quest.CanClaim(mc.thePlayer, choiceData);
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
		
		desc.drawScreen(mx, my, partialTick);
		
		RenderUtils.DrawLine(this.guiLeft + sizeX/2, this.guiTop + 32, this.guiLeft + sizeX/2, this.guiTop + sizeY - 24, 1, ThemeRegistry.curTheme().textColor());
		
		/*int tx1 = guiLeft + sizeX/2 + 8;
		int ty1 = guiTop + 48;
		int tw = sizeX/2 - 24;
		int th = sizeY - 96;
		int tx2 = tx1 + tw;
		int ty2 = ty1 + th;
		RenderUtils.DrawLine(tx1, ty1, tx2, ty2, 2, ThemeRegistry.curTheme().textColor());
		RenderUtils.DrawLine(tx1, ty1, tx2, ty1, 2, ThemeRegistry.curTheme().textColor());
		RenderUtils.DrawLine(tx1, ty2, tx2, ty2, 2, ThemeRegistry.curTheme().textColor());
		this.fontRendererObj.drawString("| TEXT |", this.guiLeft + (sizeX/4)*3, this.guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB());*/
		
		TaskBase task = selTask < quest.tasks.size()? quest.tasks.get(selTask) : null;
		
		if(task != null)
		{
			String tTitle = task.getDisplayName();
			
			if(quest.tasks.size() > 1)
			{
				tTitle = (selTask + 1) + "/" + quest.tasks.size() + " " + tTitle;
			}
			
			tTitle = EnumChatFormatting.UNDERLINE + tTitle;
			
			int nameWidth = this.fontRendererObj.getStringWidth(tTitle);
			this.fontRendererObj.drawString(tTitle, this.guiLeft + (sizeX/4)*3 - (nameWidth/2), this.guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB());
			
			if(taskRender == null)
			{
				taskRender = task.getGui(quest, this, guiLeft + sizeX/2 + 8, guiTop + 48, sizeX/2 - 24, sizeY - 104);
			}
			
			if(taskRender != null)
			{
				GL11.glPushMatrix(); 
				GL11.glColor4f(1F, 1F, 1F, 1F);
				taskRender.drawGui(mx, my, partialTick);
				GL11.glPopMatrix();
			}
		} else
		{
			taskRender = null;
		}
		
		/*int rx1 = guiLeft + 16;
		int ry1 = guiTop + sizeY/2;
		int rw = sizeX/2 - 24;
		int rh = sizeY/2 - 48;
		int rx2 = rx1 + rw;
		int ry2 = ry1 + rh;
		RenderUtils.DrawLine(rx1, ry1, rx2, ry2, 2, ThemeRegistry.curTheme().textColor());
		RenderUtils.DrawLine(rx1, ry1, rx2, ry1, 2, ThemeRegistry.curTheme().textColor());
		RenderUtils.DrawLine(rx1, ry2, rx2, ry2, 2, ThemeRegistry.curTheme().textColor());
		this.fontRendererObj.drawString("| TEXT |", guiLeft + (sizeX/4)*1, guiTop + sizeY/2 - 12, ThemeRegistry.curTheme().textColor().getRGB());*/
		
		RewardBase reward = selReward < quest.rewards.size()? quest.rewards.get(selReward) : null;
		
		if(reward != null)
		{
			String rTitle = reward.getDisplayName();
			
			if(quest.rewards.size() > 1)
			{
				rTitle = (selReward + 1) + "/" + quest.rewards.size() + " " + rTitle;
			}
			
			rTitle = EnumChatFormatting.UNDERLINE + rTitle;
			
			int nameWidth = this.fontRendererObj.getStringWidth(rTitle);
			this.fontRendererObj.drawString(rTitle, guiLeft + (sizeX/4)*1 - (nameWidth/2), guiTop + sizeY/2 - 12, ThemeRegistry.curTheme().textColor().getRGB());
			
			if(rewardRender == null)
			{
				rewardRender = reward.getGui(this, guiLeft + 16, guiTop + sizeY/2, sizeX/2 - 24, sizeY/2 - 48);
			}
			
			if(rewardRender != null)
			{
				GL11.glPushMatrix(); 
				GL11.glColor4f(1F, 1F, 1F, 1F);
				rewardRender.drawGui(mx, my, partialTick);
				GL11.glPopMatrix();
			}
		} else
		{
			rewardRender = null;
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
			taskRender = null;
		} else if(btn.id == 2) // Manual detect
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("questID", quest.questID);
			PacketAssembly.SendToServer(BQPacketType.DETECT.GetLocation(), tags);
		} else if(btn.id == 3) // Task right
		{
			selTask++;
			btnTLeft.enabled = selTask > 0;
			btnTRight.enabled = selTask < quest.tasks.size() - 1;
			taskRender = null;
		} else if(btn.id == 4) // Edit Quest
		{
			mc.displayGuiScreen(new GuiQuestEditor(this, quest));
		} else if(btn.id == 5) // Claim reward
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("questID", quest.questID);
			tags.setTag("ChoiceData", quest.GetChoiceData());
			PacketAssembly.SendToServer(BQPacketType.CLAIM.GetLocation(), tags);
		} else if(btn.id == 6)
		{
			selReward--;
			btnRLeft.enabled = selReward > 0;
			btnRRight.enabled = selReward < quest.rewards.size() - 1;
			rewardRender = null;
		} else if(btn.id == 7)
		{
			selReward++;
			btnRLeft.enabled = selReward > 0;
			btnRRight.enabled = selReward < quest.rewards.size() - 1;
			rewardRender = null;
		}
	}
	
	@Override
    protected void keyTyped(char character, int keyCode)
    {
        super.keyTyped(character, keyCode);
        
		if(taskRender != null)
		{
			taskRender.keyTyped(character, keyCode);
		}
		
		if(rewardRender != null)
		{
			rewardRender.keyTyped(character, keyCode);
		}
		
		choiceData = quest.GetChoiceData();
		btnClaim.enabled = quest.CanClaim(mc.thePlayer, choiceData);
    }
	
	@Override
	public void handleMouseInput()
	{
		if(taskRender != null)
		{
			taskRender.handleMouse();
		}
		
		if(rewardRender != null)
		{
			rewardRender.handleMouse();
		}
		
		super.handleMouseInput();
		
		choiceData = quest.GetChoiceData();
		btnClaim.enabled = quest.CanClaim(mc.thePlayer, choiceData);
	}
}
