package betterquesting.client.gui.editors;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.quests.rewards.RewardRegistry;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRewardEditor extends GuiQuesting
{
	RewardBase lastReward = null;
	JsonObject lastEdit = null;
	QuestInstance quest;
	int leftScroll = 0;
	int rightScroll = 0;
	int maxRows = 0;
	
	public GuiRewardEditor(GuiScreen parent, QuestInstance quest)
	{
		super(parent, "Reward Editor - " + quest.name);
		this.quest = quest;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		if(lastEdit != null && lastReward != null)
		{
			if(QuestDatabase.questDB.containsValue(quest) && quest.rewards.contains(lastReward))
			{
				lastReward.readFromJson(lastEdit);
				SendChanges();
			}
		}
		
		lastEdit = null;
		lastReward = null;
		
		maxRows = (sizeY - 64)/20;
		int btnWidth = Math.min(sizeX/2 - 24, 198);
		
		// Left main buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4 - (btnWidth/2 + 4) + 20, guiTop + 32 + (i*20), btnWidth - 20, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Left delete buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4 - (btnWidth/2 + 4), guiTop + 32 + (i*20), 20, 20, "" + ChatFormatting.RED + ChatFormatting.BOLD + "x");
			this.buttonList.add(btn);
		}
		
		// Right main buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4*3 - (btnWidth/2 + 4), guiTop + 32 + (i*20), btnWidth, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(QuestDatabase.updateUI)
		{
			if(!QuestDatabase.questDB.containsValue(quest))
			{
				mc.displayGuiScreen(parent);
				return;
			}
			
			QuestDatabase.updateUI = false;
			RefreshColumns();
		}
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(guiTexture);
		
		int btnWidth = Math.min(sizeX/2 - 24, 198);
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32 + (int)Math.max(0, s * (float)leftScroll/(quest.rewards.size() - maxRows)), 248, 60, 8, 20);
		
		// Right scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 32, 248, 0, 8, 20);
		s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 32 + (int)Math.max(0, s * (float)rightScroll/(RewardRegistry.GetTypeList().size() - maxRows + 1F)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, Color.BLACK);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		int n1 = button.id - 1; // Reward index
		int n2 = n1/maxRows; // Reward listing (0 = quest, 1 = quest delete, 2 = registry)
		int n3 = n1%maxRows + leftScroll; // Quest list index
		int n4 = n1%maxRows + rightScroll; // Registry list index
		
		if(n2 == 0) // Edit reward
		{
			if(n3 >= 0 && n3 < quest.rewards.size())
			{
				lastEdit = new JsonObject();
				lastReward = quest.rewards.get(n3);
				lastReward.writeToJson(lastEdit);
				mc.displayGuiScreen(lastReward.GetEditor(this, lastEdit));
			}
		} else if(n2 == 1) // Delete reward
		{
			if(!(n3 < 0 || n3 >= quest.rewards.size()))
			{
				quest.rewards.remove(n3);
				SendChanges();
			}
		} else if(n2 == 2) // Add reward
		{
			if(!(n4 < 0 || n4 >= RewardRegistry.GetTypeList().size()))
			{
				quest.rewards.add(RewardRegistry.InstatiateReward(RewardRegistry.GetTypeList().get(n4)));
				SendChanges();
			}
		}
	}
	
    /**
     * Handles mouse input.
     */
	@Override
    public void handleMouseInput()
    {
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll + SDX, 0, quest.rewards.size() - maxRows));
    		RefreshColumns();
        }
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
        	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + SDX, 0, RewardRegistry.GetTypeList().size() - maxRows));
        	RefreshColumns();
        }
    }
	
	public void SendChanges()
	{
		JsonObject json = new JsonObject();
		quest.writeToJSON(json);
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 5);
		tags.setInteger("action", 0); // Action: Update data
		tags.setInteger("questID", quest.questID);
		tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
	}
	
	public void RefreshColumns()
	{
    	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, RewardRegistry.GetTypeList().size() - maxRows));
		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll, 0, quest.rewards.size() - maxRows));
		
		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 1; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = i - 1; // Reward index
			int n2 = n1/maxRows; // Reward listing (0 = quest, 1 = quest delete, 2 = registry)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			int n4 = n1%maxRows + rightScroll; // Registry list index
			
			if(n2 == 0) // Edit reward
			{
				if(n3 < 0 || n3 >= quest.rewards.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					btn.visible = btn.enabled = true;
					btn.displayString = quest.rewards.get(n3).getDisplayName();
				}
			} else if(n2 == 1) // Delete reward
			{
				btn.visible = btn.enabled = !(n3 < 0 || n3 >= quest.rewards.size());
			} else if(n2 == 2) // Add reward
			{
				if(n4 < 0 || n4 >= RewardRegistry.GetTypeList().size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					btn.visible = btn.enabled = true;
					btn.displayString = RewardRegistry.GetTypeList().get(n4);
				}
			}
		}
	}
}
