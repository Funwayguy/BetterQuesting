package betterquesting.client.gui.editors;

import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.quests.tasks.TaskRegistry;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTaskEditor extends GuiQuesting implements IVolatileScreen
{
	TaskBase lastTask = null;
	JsonObject lastEdit = null;
	QuestInstance quest;
	int leftScroll = 0;
	int rightScroll = 0;
	int maxRows = 0;
	
	public GuiTaskEditor(GuiScreen parent, QuestInstance quest)
	{
		super(parent, I18n.format("betterquesting.title.edit_tasks", I18n.format(quest.name)));
		this.quest = quest;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		if(lastEdit != null && lastTask != null)
		{
			if(QuestDatabase.questDB.containsValue(quest) && quest.tasks.contains(lastTask))
			{
				lastTask.readFromJson(lastEdit);
				SendChanges();
			}
		}
		
		lastEdit = null;
		lastTask = null;
		
		maxRows = (sizeY - 64)/20;
		int btnWidth = sizeX/2 - 16;
		
		// Left main buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 36, guiTop + 32 + (i*20), btnWidth - 36, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Left delete buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 16, guiTop + 32 + (i*20), 20, 20, "" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "x");
			this.buttonList.add(btn);
		}
		
		// Right main buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/2 + 8, guiTop + 32 + (i*20), btnWidth - 16, 20, "NULL");
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
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + (int)Math.max(0, s * (float)leftScroll/(quest.tasks.size() - maxRows)), 248, 60, 8, 20);
		
		// Right scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 32, 248, 0, 8, 20);
		s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 32 + (int)Math.max(0, s * (float)rightScroll/(TaskRegistry.GetNameList().size() - maxRows)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, ThemeRegistry.curTheme().textColor());
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
			if(n3 >= 0 && n3 < quest.tasks.size())
			{
				lastEdit = new JsonObject();
				lastTask = quest.tasks.get(n3);
				lastTask.writeToJson(lastEdit);
				mc.displayGuiScreen(lastTask.GetEditor(this, lastEdit));
			}
		} else if(n2 == 1) // Delete reward
		{
			if(!(n3 < 0 || n3 >= quest.tasks.size()))
			{
				quest.tasks.remove(n3);
				SendChanges();
			}
		} else if(n2 == 2) // Add reward
		{
			if(!(n4 < 0 || n4 >= TaskRegistry.GetNameList().size()))
			{
				quest.tasks.add(TaskRegistry.InstatiateTask(TaskRegistry.GetNameList().get(n4)));
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
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll + SDX, 0, quest.tasks.size() - maxRows));
    		RefreshColumns();
        }
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
        	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + SDX, 0, TaskRegistry.GetNameList().size() - maxRows));
        	RefreshColumns();
        }
    }
	
	public void SendChanges()
	{
		JsonObject json1 = new JsonObject();
		quest.writeToJSON(json1);
		JsonObject json2 = new JsonObject();
		quest.writeProgressToJSON(json2);
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", 0); // Action: Update data
		tags.setInteger("questID", quest.questID);
		tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json1, new NBTTagCompound()));
		tags.setTag("Progress", NBTConverter.JSONtoNBT_Object(json2, new NBTTagCompound()));
		PacketAssembly.SendToServer(BQPacketType.QUEST_EDIT.GetLocation(), tags);
	}
	
	public void RefreshColumns()
	{
    	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, TaskRegistry.GetNameList().size() - maxRows));
		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll, 0, quest.tasks.size() - maxRows));
		
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
				if(n3 < 0 || n3 >= quest.tasks.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					btn.visible = btn.enabled = true;
					btn.displayString = quest.tasks.get(n3).getDisplayName();
				}
			} else if(n2 == 1) // Delete reward
			{
				btn.visible = btn.enabled = !(n3 < 0 || n3 >= quest.tasks.size());
			} else if(n2 == 2) // Add reward
			{
				if(n4 < 0 || n4 >= TaskRegistry.GetNameList().size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					btn.visible = btn.enabled = true;
					btn.displayString = TaskRegistry.GetNameList().get(n4).toString();
				}
			}
		}
	}
}
