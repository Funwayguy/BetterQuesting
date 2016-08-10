package betterquesting.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.INeedsRefresh;
import betterquesting.api.client.gui.premade.controls.GuiButtonThemed;
import betterquesting.api.client.gui.premade.screens.GuiScreenThemed;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.quests.rewards.IRewardBase;
import betterquesting.api.quests.tasks.ITaskBase;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.editors.GuiQuestEditor;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiScrollingText;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.registry.ThemeRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestInstance extends GuiScreenThemed implements INeedsRefresh
{
	int questId = -1;
	IQuestContainer quest;
	int selTask = 0;
	IGuiEmbedded taskRender = null;
	int selReward = 0;
	IGuiEmbedded rewardRender = null;
	GuiScrollingText desc;
	GuiButtonThemed btnTLeft;
	GuiButtonThemed btnTRight;
	GuiButtonThemed btnRLeft;
	GuiButtonThemed btnRRight;
	GuiButtonThemed btnClaim;
	
	/**
	 * Cached between UI updates
	 */
	NBTTagList choiceData = new NBTTagList();
	
	public GuiQuestInstance(GuiScreen parent, int questId)
	{
		super(parent, "?");
		this.questId = questId;
	}
	
	@Override
	public void refreshGui()
	{
		this.quest = QuestDatabase.INSTANCE.getQuest(questId);
		
		if(quest == null)
		{
			this.mc.displayGuiScreen(parent);
		} else
		{
			this.setTitle(I18n.format(quest.getUnlocalisedName()));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();

		this.quest = QuestDatabase.INSTANCE.getQuest(questId);
		
		if(quest == null)
		{
			this.mc.displayGuiScreen(parent);
		} else
		{
			this.setTitle(I18n.format(quest.getUnlocalisedName()));
			desc.SetText(I18n.format(quest.getUnlocalisedDescription()));
		}
		
		this.selReward = 0;
		this.taskRender = null;
		this.selTask = 0;
		this.rewardRender = null;
		
		if(QuestDatabase.editMode)
		{
			((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
			((GuiButton)this.buttonList.get(0)).width = 100;
		}
		
		GuiButtonQuesting btnEdit = new GuiButtonQuesting(4, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, I18n.format("betterquesting.btn.edit"));
		btnEdit.enabled = btnEdit.visible = QuestDatabase.editMode;
		this.buttonList.add(btnEdit);
		
		desc = new GuiScrollingText(this.guiLeft + 16, this.guiTop + 32, sizeX/2 - 24, quest.getAllRewards().size() > 0? sizeY/2 - 48 : sizeY - 64, I18n.format(quest.getUnlocalisedDescription()));
		
		btnTLeft = new GuiButtonThemed(1, this.guiLeft + (sizeX/4)*3 - 70, this.guiTop + sizeY - 48, 20, 20, "<", true);
		btnTLeft.enabled = selTask > 0;
		btnTRight = new GuiButtonThemed(3, this.guiLeft + (sizeX/4)*3 + 50, this.guiTop + sizeY - 48, 20, 20, ">", true);
		btnTRight.enabled = selTask < quest.getAllTasks().size() - 1;

		btnRLeft = new GuiButtonThemed(6, this.guiLeft + (sizeX/4) - 70, this.guiTop + sizeY - 48, 20, 20, "<", true);
		btnRLeft.visible = quest.getAllRewards().size() > 0;
		btnRLeft.enabled = btnRLeft.visible && selReward > 0;
		btnRRight = new GuiButtonThemed(7, this.guiLeft + (sizeX/4) + 50, this.guiTop + sizeY - 48, 20, 20, ">", true);
		btnRRight.visible = quest.getAllRewards().size() > 0;
		btnRRight.enabled = btnRRight.visible && selReward < quest.getAllRewards().size() - 1;
		
		GuiButtonQuesting btnDetect = new GuiButtonQuesting(2, this.guiLeft + (sizeX/4)*3 - 50, this.guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.detect_submit"));
		btnDetect.enabled = quest.canSubmit(mc.thePlayer);
		btnClaim = new GuiButtonThemed(5, this.guiLeft + (sizeX/4) - 50, this.guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.claim"), true);
		btnClaim.visible = quest.getAllRewards().size() > 0;
		btnClaim.enabled = btnClaim.visible && quest.canClaim(mc.thePlayer);
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
		
		desc.drawScreen(mx, my, partialTick);
		
		RenderUtils.DrawLine(this.guiLeft + sizeX/2, this.guiTop + 32, this.guiLeft + sizeX/2, this.guiTop + sizeY - 24, 1, getTextColor());
		
		ITaskBase task = quest.getTask(selTask);
		
		if(task != null)
		{
			String tTitle = I18n.format(task.getUnlocalisedName());
			
			if(quest.getAllTasks().size() > 1)
			{
				tTitle = (selTask + 1) + "/" + quest.getAllTasks().size() + " " + tTitle;
			}
			
			tTitle = EnumChatFormatting.UNDERLINE + tTitle;
			
			int nameWidth = this.fontRendererObj.getStringWidth(tTitle);
			this.fontRendererObj.drawString(tTitle, this.guiLeft + (sizeX/4)*3 - (nameWidth/2), this.guiTop + 32, getTextColor());
			
			if(taskRender == null)
			{
				taskRender = task.getTaskGui(guiLeft + sizeX/2 + 8, guiTop + 48, sizeX/2 - 24, sizeY - 104, quest);
			}
		} else
		{
			taskRender = null;
		}
		
		IRewardBase reward = selReward < quest.getAllRewards().size()? quest.getAllRewards().get(selReward) : null;
		
		if(reward != null)
		{
			String rTitle = reward.getUnlocalisedName();
			
			if(quest.getAllRewards().size() > 1)
			{
				rTitle = (selReward + 1) + "/" + quest.getAllRewards().size() + " " + rTitle;
			}
			
			rTitle = EnumChatFormatting.UNDERLINE + rTitle;
			
			int nameWidth = this.fontRendererObj.getStringWidth(rTitle);
			this.fontRendererObj.drawString(rTitle, guiLeft + (sizeX/4)*1 - (nameWidth/2), guiTop + sizeY/2 - 12, getTextColor());
			
			if(rewardRender == null)
			{
				rewardRender = reward.getRewardGui(guiLeft + 16, guiTop + sizeY/2, sizeX/2 - 24, sizeY/2 - 48, quest);
			}
		} else
		{
			rewardRender = null;
		}
		
		if(taskRender != null)
		{
			GL11.glPushMatrix(); 
			GL11.glColor4f(1F, 1F, 1F, 1F);
			taskRender.drawBackground(mx, my, partialTick);
			GL11.glPopMatrix();
		}
		
		if(rewardRender != null)
		{
			GL11.glPushMatrix(); 
			GL11.glColor4f(1F, 1F, 1F, 1F);
			rewardRender.drawBackground(mx, my, partialTick);
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
			btnTRight.enabled = selTask < quest.getAllTasks().size() - 1;
			taskRender = null;
		} else if(btn.id == 2) // Manual detect
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("questID", QuestDatabase.INSTANCE.getQuestID(quest));
			PacketSender.INSTANCE.sendToServer(PacketTypeNative.DETECT.GetLocation(), tags);
		} else if(btn.id == 3) // Task right
		{
			selTask++;
			btnTLeft.enabled = selTask > 0;
			btnTRight.enabled = selTask < quest.getAllTasks().size() - 1;
			taskRender = null;
		} else if(btn.id == 4) // Edit Quest
		{
			mc.displayGuiScreen(new GuiQuestEditor(this, quest));
		} else if(btn.id == 5) // Claim reward
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("questID", QuestDatabase.INSTANCE.getQuestID(quest));
			tags.setTag("ChoiceData", quest.GetChoiceData());
			PacketSender.INSTANCE.sendToServer(PacketTypeNative.CLAIM.GetLocation(), tags);
		} else if(btn.id == 6)
		{
			selReward--;
			btnRLeft.enabled = selReward > 0;
			btnRRight.enabled = selReward < quest.getAllRewards().size() - 1;
			rewardRender = null;
		} else if(btn.id == 7)
		{
			selReward++;
			btnRLeft.enabled = selReward > 0;
			btnRRight.enabled = selReward < quest.getAllRewards().size() - 1;
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
		
		btnClaim.enabled = quest.canClaim(mc.thePlayer);
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
