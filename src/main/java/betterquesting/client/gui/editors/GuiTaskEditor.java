package betterquesting.client.gui.editors;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.INeedsRefresh;
import betterquesting.api.client.gui.IVolatileScreen;
import betterquesting.api.client.gui.premade.controls.GuiButtonThemed;
import betterquesting.api.client.gui.premade.screens.GuiScreenThemed;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.api.utils.IFactory;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestDatabase;
import betterquesting.registry.TaskRegistry;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTaskEditor extends GuiScreenThemed implements IVolatileScreen, INeedsRefresh
{
	private List<IFactory<ITask>> taskTypes = new ArrayList<IFactory<ITask>>();
	private List<Integer> taskIDs = new ArrayList<Integer>();
	private IQuest quest;
	private int qId = -1;
	
	private int leftScroll = 0;
	private int rightScroll = 0;
	private int maxRows = 0;
	
	public GuiTaskEditor(GuiScreen parent, IQuest quest)
	{
		super(parent, I18n.format("betterquesting.title.edit_tasks", I18n.format(quest.getUnlocalisedName())));
		this.quest = quest;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		taskTypes = TaskRegistry.INSTANCE.getAll();
		taskIDs = quest.getTasks().getAllKeys();
		
		maxRows = (sizeY - 64)/20;
		int btnWidth = sizeX/2 - 16;
		
		// Left main buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + 36, guiTop + 32 + (i*20), btnWidth - 36, 20, "NULL", true);
			this.buttonList.add(btn);
		}
		
		// Left delete buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + 16, guiTop + 32 + (i*20), 20, 20, "" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "x", true);
			this.buttonList.add(btn);
		}
		
		// Right main buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + sizeX/2 + 8, guiTop + 32 + (i*20), btnWidth - 16, 20, "NULL", true);
			this.buttonList.add(btn);
		}
		
		RefreshColumns();
	}
	
	@Override
	public void refreshGui()
	{
		IQuest tmp = QuestDatabase.INSTANCE.getValue(qId);
		
		if(tmp == null)
		{
			mc.displayGuiScreen(parent);
			return;
		}
		
		this.quest = tmp;
		this.taskIDs = quest.getTasks().getAllKeys();
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + (int)Math.max(0, s * (float)leftScroll/(taskIDs.size() - maxRows)), 248, 60, 8, 20);
		
		// Right scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 32, 248, 0, 8, 20);
		s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 32 + (int)Math.max(0, s * (float)rightScroll/(taskTypes.size() - maxRows)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, getTextColor());
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
			if(n3 >= 0 && n3 < taskIDs.size())
			{
				GuiScreen editor = quest.getTasks().getValue(taskIDs.get(n3)).getTaskEditor(this, quest);
				
				if(editor != null)
				{
					mc.displayGuiScreen(editor);
				}
			}
		} else if(n2 == 1) // Delete reward
		{
			if(!(n3 < 0 || n3 >= taskIDs.size()))
			{
				quest.getTasks().removeKey(taskIDs.get(n3));
				SendChanges();
			}
		} else if(n2 == 2) // Add reward
		{
			if(!(n4 < 0 || n4 >= taskTypes.size()))
			{
				quest.getTasks().add(TaskRegistry.INSTANCE.createTask(taskTypes.get(n4).getRegistryName()), quest.getTasks().nextKey());
				SendChanges();
			}
		}
	}
	
	@Override
	public void mouseScroll(int mx, int my, int scroll)
	{
		super.mouseScroll(mx, my, scroll);
        
        if(scroll != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll + scroll, 0, quest.getTasks().size() - maxRows));
    		RefreshColumns();
        }
        
        if(scroll != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
        	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + scroll, 0, taskTypes.size() - maxRows));
        	RefreshColumns();
        }
	}
	
	public void SendChanges()
	{
		JsonObject base = new JsonObject();
		base.add("config", quest.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		base.add("progress", quest.writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
		tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(quest));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}
	
	public void RefreshColumns()
	{
    	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, taskTypes.size() - maxRows));
		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll, 0, quest.getTasks().size() - maxRows));
		
		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 1; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = i - 1; // Task index
			int n2 = n1/maxRows; // Task listing (0 = task, 1 = task delete, 2 = registry)
			int n3 = n1%maxRows + leftScroll; // Task list index
			int n4 = n1%maxRows + rightScroll; // Registry list index
			
			if(n2 == 0) // Edit task
			{
				if(n3 < 0 || n3 >= taskIDs.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					btn.visible = btn.enabled = true;
					btn.displayString = I18n.format(quest.getTasks().getValue(taskIDs.get(n3)).getUnlocalisedName());
				}
			} else if(n2 == 1) // Delete task
			{
				btn.visible = btn.enabled = !(n3 < 0 || n3 >= taskIDs.size());
			} else if(n2 == 2) // Add task
			{
				if(n4 < 0 || n4 >= taskTypes.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					btn.visible = btn.enabled = true;
					btn.displayString = taskTypes.get(n4).toString();
				}
			}
		}
	}
}
