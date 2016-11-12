package betterquesting.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingText;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.rewards.IReward;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.editors.GuiQuestEditor;
import betterquesting.database.QuestDatabase;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestSettings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestInstance extends GuiScreenThemed implements INeedsRefresh
{
	int id = -1;
	IQuest quest;
	int selTask = 0;
	IGuiEmbedded taskRender = null;
	int selReward = 0;
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
		this.quest = QuestDatabase.INSTANCE.getValue(id);
		
		if(quest == null)
		{
			this.mc.displayGuiScreen(parent);
			return;
		}
		
		initGui();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		this.selReward = 0;
		this.taskRender = null;
		this.selTask = 0;
		this.rewardRender = null;
		
		if(QuestSettings.INSTANCE.canUserEdit(mc.thePlayer))
		{
			((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
			((GuiButton)this.buttonList.get(0)).width = 100;
		}
		
		GuiButtonThemed btnEdit = new GuiButtonThemed(4, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, I18n.format("betterquesting.btn.edit"), true);
		btnEdit.enabled = btnEdit.visible = QuestSettings.INSTANCE.canUserEdit(mc.thePlayer);
		this.buttonList.add(btnEdit);
		
		this.setTitle(I18n.format(quest.getUnlocalisedName()));
		this.embedded.add(new GuiScrollingText(mc, this.guiLeft + 16, this.guiTop + 32, sizeX/2 - 24, quest.getRewards().size() > 0? sizeY/2 - 48 : sizeY - 64, I18n.format(quest.getUnlocalisedDescription())));
		
		btnTLeft = new GuiButtonThemed(1, this.guiLeft + (sizeX/4)*3 - 70, this.guiTop + sizeY - 48, 20, 20, "<", true);
		btnTLeft.enabled = selTask > 0;
		btnTRight = new GuiButtonThemed(3, this.guiLeft + (sizeX/4)*3 + 50, this.guiTop + sizeY - 48, 20, 20, ">", true);
		btnTRight.enabled = selTask < quest.getTasks().size() - 1;

		btnRLeft = new GuiButtonThemed(6, this.guiLeft + (sizeX/4) - 70, this.guiTop + sizeY - 48, 20, 20, "<", true);
		btnRLeft.visible = quest.getRewards().size() > 0;
		btnRLeft.enabled = btnRLeft.visible && selReward > 0;
		btnRRight = new GuiButtonThemed(7, this.guiLeft + (sizeX/4) + 50, this.guiTop + sizeY - 48, 20, 20, ">", true);
		btnRRight.visible = quest.getRewards().size() > 0;
		btnRRight.enabled = btnRRight.visible && selReward < quest.getRewards().size() - 1;
		
		GuiButtonThemed btnDetect = new GuiButtonThemed(2, this.guiLeft + (sizeX/4)*3 - 50, this.guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.detect_submit"), true);
		btnDetect.enabled = quest.canSubmit(mc.thePlayer);
		btnClaim = new GuiButtonThemed(5, this.guiLeft + (sizeX/4) - 50, this.guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.claim"), true);
		btnClaim.visible = quest.getRewards().size() > 0;
		btnClaim.enabled = btnClaim.visible && quest.canClaim(mc.thePlayer);
		this.buttonList.add(btnTLeft);
		this.buttonList.add(btnTRight);
		this.buttonList.add(btnRLeft);
		this.buttonList.add(btnRRight);
		this.buttonList.add(btnDetect);
		this.buttonList.add(btnClaim);
		
		refreshEmbedded();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		RenderUtils.DrawLine(this.guiLeft + sizeX/2, this.guiTop + 32, this.guiLeft + sizeX/2, this.guiTop + sizeY - 24, 1, getTextColor());
		
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
			btnTRight.enabled = selTask < quest.getTasks().size() - 1;
			this.embedded.remove(taskRender);
			taskRender = null;
			refreshEmbedded();
		} else if(btn.id == 2) // Manual detect
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(quest));
			PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.DETECT.GetLocation(), tags));
		} else if(btn.id == 3) // Task right
		{
			selTask++;
			btnTLeft.enabled = selTask > 0;
			btnTRight.enabled = selTask < quest.getTasks().size() - 1;
			this.embedded.remove(taskRender);
			taskRender = null;
			refreshEmbedded();
		} else if(btn.id == 4) // Edit Quest
		{
			mc.displayGuiScreen(new GuiQuestEditor(this, quest));
		} else if(btn.id == 5) // Claim reward
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(quest));
			PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.CLAIM.GetLocation(), tags));
		} else if(btn.id == 6)
		{
			selReward--;
			btnRLeft.enabled = selReward > 0;
			btnRRight.enabled = selReward < quest.getRewards().size() - 1;
			this.embedded.remove(rewardRender);
			rewardRender = null;
			refreshEmbedded();
		} else if(btn.id == 7)
		{
			selReward++;
			btnRLeft.enabled = selReward > 0;
			btnRRight.enabled = selReward < quest.getRewards().size() - 1;
			this.embedded.remove(rewardRender);
			rewardRender = null;
			refreshEmbedded();
		}
	}
	
	@Override
    protected void keyTyped(char character, int keyCode)
    {
        super.keyTyped(character, keyCode);
        
		btnClaim.enabled = quest.canClaim(mc.thePlayer);
    }
	
	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();
		
		btnClaim.enabled = quest.canClaim(mc.thePlayer);
	}
	
	private void refreshEmbedded()
	{
		int tSize = quest.getTasks().size();
		if(taskRender == null && tSize > 0)
		{
			ITask task = quest.getTasks().getAllValues().get(selTask%tSize);
			
			if(task != null)
			{
				String tTitle = I18n.format(task.getUnlocalisedName());
				
				if(tSize > 1)
				{
					tTitle = (selTask + 1) + "/" + tSize + " " + tTitle;
				}
				
				tTitle = EnumChatFormatting.UNDERLINE + tTitle;
				
				int nameWidth = this.fontRendererObj.getStringWidth(tTitle);
				this.fontRendererObj.drawString(tTitle, this.guiLeft + (sizeX/4)*3 - (nameWidth/2), this.guiTop + 32, getTextColor());
				taskRender = task.getTaskGui(guiLeft + sizeX/2 + 8, guiTop + 48, sizeX/2 - 24, sizeY - 104, quest);
			}
		}
		
		int rSize = quest.getRewards().size();
		if(rewardRender == null && rSize > 0)
		{
			IReward reward = quest.getRewards().getAllValues().get(selReward%rSize);
			
			if(reward != null)
			{
				String rTitle = reward.getUnlocalisedName();
				
				if(rSize > 1)
				{
					rTitle = (selReward + 1) + "/" + rSize + " " + rTitle;
				}
				
				rTitle = EnumChatFormatting.UNDERLINE + rTitle;
				
				int nameWidth = this.fontRendererObj.getStringWidth(rTitle);
				this.fontRendererObj.drawString(rTitle, guiLeft + (sizeX/4)*1 - (nameWidth/2), guiTop + sizeY/2 - 12, getTextColor());
				rewardRender = reward.getRewardGui(guiLeft + 16, guiTop + sizeY/2, sizeX/2 - 24, sizeY/2 - 48, quest);
			}
		}
	}
}
