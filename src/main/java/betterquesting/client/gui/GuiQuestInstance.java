package betterquesting.client.gui;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingText;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.editors.GuiQuestEditor;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.QuestSettings;

@SideOnly(Side.CLIENT)
public class GuiQuestInstance extends GuiScreenThemed implements INeedsRefresh
{
	int id = -1;
	IQuest quest;
	
	int selTaskId = 0;
	ITask selTask = null;
	IGuiEmbedded taskRender = null;
	
	int selRewardId = 0;
	IReward selReward = null;
	IGuiEmbedded rewardRender = null;
	
	GuiButtonThemed btnTLeft;
	GuiButtonThemed btnTRight;
	GuiButtonThemed btnRLeft;
	GuiButtonThemed btnRRight;
	GuiButtonThemed btnClaim;
	
	/**
	 * Cached between UI updates
	 */
	NBTTagList choiceData = new NBTTagList();
	
	public GuiQuestInstance(GuiScreen parent, IQuest quest)
	{
		super(parent, I18n.format(quest.getUnlocalisedName()));
		this.quest = quest;
		this.id = QuestDatabase.INSTANCE.getKey(quest);
	}
	
	@Override
	public void refreshGui()
	{
		IQuest tmp = QuestDatabase.INSTANCE.getValue(id);
		
		if(tmp == quest)
		{
			return;
		}
		
		this.quest = tmp;
		
		if(quest == null)
		{
			this.mc.displayGuiScreen(parent);
			return;
		}
		
		initGui();
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		if(QuestSettings.INSTANCE.canUserEdit(mc.player))
		{
			((GuiButton)this.buttonList.get(0)).x = this.width/2 - 100;
			((GuiButton)this.buttonList.get(0)).width = 100;
		}
		
		GuiButtonThemed btnEdit = new GuiButtonThemed(4, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, I18n.format("betterquesting.btn.edit"), true);
		btnEdit.enabled = btnEdit.visible = QuestSettings.INSTANCE.canUserEdit(mc.player);
		this.buttonList.add(btnEdit);
		
		this.setTitle(I18n.format(quest.getUnlocalisedName()));
		this.embedded.add(new GuiScrollingText(mc, this.guiLeft + 16, this.guiTop + 32, sizeX/2 - 24, quest.getRewards().size() > 0? sizeY/2 - 48 : sizeY - 64, I18n.format(quest.getUnlocalisedDescription())));
		
		btnTLeft = new GuiButtonThemed(1, this.guiLeft + (sizeX/4)*3 - 70, this.guiTop + sizeY - 48, 20, 20, "<", true);
		btnTLeft.enabled = selTaskId > 0;
		btnTRight = new GuiButtonThemed(3, this.guiLeft + (sizeX/4)*3 + 50, this.guiTop + sizeY - 48, 20, 20, ">", true);
		btnTRight.enabled = selTaskId < quest.getTasks().size() - 1;

		btnRLeft = new GuiButtonThemed(6, this.guiLeft + (sizeX/4) - 70, this.guiTop + sizeY - 48, 20, 20, "<", true);
		btnRLeft.visible = quest.getRewards().size() > 0;
		btnRLeft.enabled = btnRLeft.visible && selRewardId > 0;
		btnRRight = new GuiButtonThemed(7, this.guiLeft + (sizeX/4) + 50, this.guiTop + sizeY - 48, 20, 20, ">", true);
		btnRRight.visible = quest.getRewards().size() > 0;
		btnRRight.enabled = btnRRight.visible && selRewardId < quest.getRewards().size() - 1;
		
		GuiButtonThemed btnDetect = new GuiButtonThemed(2, this.guiLeft + (sizeX/4)*3 - 50, this.guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.detect_submit"), true);
		btnDetect.enabled = quest.canSubmit(mc.player);
		btnClaim = new GuiButtonThemed(5, this.guiLeft + (sizeX/4) - 50, this.guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.claim"), true);
		btnClaim.visible = quest.getRewards().size() > 0;
		btnClaim.enabled = btnClaim.visible && quest.canClaim(mc.player);
		this.buttonList.add(btnTLeft);
		this.buttonList.add(btnTRight);
		this.buttonList.add(btnRLeft);
		this.buttonList.add(btnRRight);
		this.buttonList.add(btnDetect);
		this.buttonList.add(btnClaim);
		
		refreshEmbedded();
	}
	
	@Override
	public void drawBackPanel(int mx, int my, float partialTick)
	{
		super.drawBackPanel(mx, my, partialTick);
		
		RenderUtils.DrawLine(this.guiLeft + sizeX/2, this.guiTop + 32, this.guiLeft + sizeX/2, this.guiTop + sizeY - 24, 1, getTextColor());
		
		if(selTask != null)
		{
			int tSize = quest.getTasks().size();
			String tTitle = I18n.format(selTask.getUnlocalisedName());
			
			if(tSize > 1)
			{
				tTitle = (selTaskId + 1) + "/" + tSize + " " + tTitle;
			}
			
			tTitle = TextFormatting.UNDERLINE + tTitle;
			
			int nameWidth = this.fontRenderer.getStringWidth(tTitle);
			
			this.fontRenderer.drawString(tTitle, this.guiLeft + (sizeX/4)*3 - (nameWidth/2), this.guiTop + 32, getTextColor());
		}
		
		if(selReward != null)
		{
			int rSize = quest.getRewards().size();
			String rTitle = I18n.format(selReward.getUnlocalisedName());
			
			if(rSize > 1)
			{
				rTitle = (selRewardId + 1) + "/" + rSize + " " + rTitle;
			}
			
			rTitle = TextFormatting.UNDERLINE + rTitle;
			
			int nameWidth = this.fontRenderer.getStringWidth(rTitle);
			this.fontRenderer.drawString(rTitle, guiLeft + (sizeX/4)*1 - (nameWidth/2), guiTop + sizeY/2 - 12, getTextColor());
		}
	}
	
	@Override
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id == 1) // Task left
		{
			selTaskId--;
			btnTLeft.enabled = selTaskId > 0;
			btnTRight.enabled = selTaskId < quest.getTasks().size() - 1;
			this.embedded.remove(taskRender);
			taskRender = null;
			refreshEmbedded();
		} else if(btn.id == 2) // Manual detect
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(quest));
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.DETECT.GetLocation(), tags));
		} else if(btn.id == 3) // Task right
		{
			selTaskId++;
			btnTLeft.enabled = selTaskId > 0;
			btnTRight.enabled = selTaskId < quest.getTasks().size() - 1;
			refreshEmbedded();
		} else if(btn.id == 4) // Edit Quest
		{
			mc.displayGuiScreen(new GuiQuestEditor(this, quest));
		} else if(btn.id == 5) // Claim reward
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(quest));
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.CLAIM.GetLocation(), tags));
		} else if(btn.id == 6)
		{
			selRewardId--;
			btnRLeft.enabled = selRewardId > 0;
			btnRRight.enabled = selRewardId < quest.getRewards().size() - 1;
			refreshEmbedded();
		} else if(btn.id == 7)
		{
			selRewardId++;
			btnRLeft.enabled = selRewardId > 0;
			btnRRight.enabled = selRewardId < quest.getRewards().size() - 1;
			refreshEmbedded();
		}
	}
	
	@Override
    protected void keyTyped(char character, int keyCode) throws IOException
    {
        super.keyTyped(character, keyCode);
        
		btnClaim.enabled = quest.canClaim(mc.player);
    }
	
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		
		btnClaim.enabled = quest.canClaim(mc.player);
	}
	
	private void refreshEmbedded()
	{
		this.embedded.remove(taskRender);
		taskRender = null;
		
		this.embedded.remove(rewardRender);
		rewardRender = null;
		
		int tSize = quest.getTasks().size();
		if(taskRender == null && tSize > 0)
		{
			selTask = quest.getTasks().getAllValues().get(selTaskId%tSize);
			
			if(selTask != null)
			{
				taskRender = selTask.getTaskGui(guiLeft + sizeX/2 + 8, guiTop + 48, sizeX/2 - 24, sizeY - 96, quest);
				
				if(taskRender != null)
				{
					this.embedded.add(taskRender);
				}
			}
		}
		
		int rSize = quest.getRewards().size();
		if(rewardRender == null && rSize > 0)
		{
			selReward = quest.getRewards().getAllValues().get(selRewardId%rSize);
			
			if(selReward != null)
			{
				rewardRender = selReward.getRewardGui(guiLeft + 16, guiTop + sizeY/2, sizeX/2 - 24, sizeY/2 - 48, quest);
				
				if(rewardRender != null)
				{
					this.embedded.add(rewardRender);
				}
			}
		}
	}
}
