package betterquesting.client.gui.editors.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import betterquesting.api2.storage.DBEntry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.RenderUtils;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.tasks.TaskRegistry;

@SideOnly(Side.CLIENT)
public class GuiTaskEditor extends GuiScreenThemed implements IVolatileScreen, INeedsRefresh
{
	private List<IFactory<? extends ITask>> taskTypes = new ArrayList<>();
	private DBEntry<ITask>[] taskIDs = new DBEntry[0];
	private IQuest quest;
	private int qId = -1;
	
	private GuiScrollingButtons btnsLeft;
	private GuiScrollingButtons btnsRight;
	
	public GuiTaskEditor(GuiScreen parent, IQuest quest)
	{
		super(parent, I18n.format("betterquesting.title.edit_tasks", I18n.format(quest.getUnlocalisedName())));
		this.quest = quest;
		this.qId = QuestDatabase.INSTANCE.getID(quest);
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		taskTypes = TaskRegistry.INSTANCE.getAll();
		taskIDs = quest.getTasks().getEntries();
		
		btnsLeft = new GuiScrollingButtons(mc, guiLeft + 16, guiTop + 32, sizeX/2 - 24, sizeY - 64);
		btnsRight = new GuiScrollingButtons(mc, guiLeft + sizeX/2 + 8, guiTop + 32, sizeX/2 - 24, sizeY - 64);
		this.embedded.add(btnsLeft);
		this.embedded.add(btnsRight);
		
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
		this.taskIDs = quest.getTasks().getEntries();
		RefreshColumns();
	}
	
	@Override
	public void drawBackPanel(int mx, int my, float partialTick)
	{
		super.drawBackPanel(mx, my, partialTick);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, getTextColor());
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		int column = button.id&3;
		int id = (button.id >> 2) - 1;
		
		if(id < 0)
		{
			return;
		}
		
		if(column == 0) // Edit reward
		{
			ITask task = quest.getTasks().getValue(id);
			GuiScreen editor = task.getTaskEditor(this, quest);
			
			if(editor != null)
			{
				mc.displayGuiScreen(editor);
			} else
			{
				mc.displayGuiScreen(new GuiTaskEditDefault(this, quest, task));
			}
		} else if(column == 1) // Delete reward
		{
			quest.getTasks().removeID(id);
			SendChanges();
		} else if(column == 2) // Add reward
		{
			if(id >= 0 && id < taskTypes.size())
			{
				quest.getTasks().add(quest.getTasks().nextID(), TaskRegistry.INSTANCE.createTask(taskTypes.get(id).getRegistryName()));
				SendChanges();
			}
		}
	}
	
	@Override
	public void mouseClicked(int mx, int my, int click) throws IOException
	{
		super.mouseClicked(mx, my, click);
		
		if(click != 0)
		{
			return;
		}
		
		GuiButtonThemed btn1 = btnsLeft.getButtonUnderMouse(mx, my);
		
		if(btn1 != null && btn1.mousePressed(mc, mx, my))
		{
			btn1.playPressSound(mc.getSoundHandler());
			this.actionPerformed(btn1);
			return;
		}
		
		GuiButtonThemed btn2 = btnsRight.getButtonUnderMouse(mx, my);
		
		if(btn2 != null && btn2.mousePressed(mc, mx, my))
		{
			btn2.playPressSound(mc.getSoundHandler());
			this.actionPerformed(btn2);
			return;
		}
	}
	
	public void SendChanges()
	{
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("config", quest.writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG));
		base.setTag("progress", quest.writeToNBT(new NBTTagCompound(), EnumSaveType.PROGRESS));
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
		tags.setInteger("questID", QuestDatabase.INSTANCE.getID(quest));
		tags.setTag("data", base);
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}
	
	public void RefreshColumns()
	{
		btnsLeft.getEntryList().clear();
		btnsRight.getEntryList().clear();
		
		for(DBEntry<ITask> tID : taskIDs)
		{
			int btnWidth = btnsLeft.getListWidth();
			int bID = (1 + tID.getID()) << 2; // First 2 bits reserved for column index
			
			GuiButtonThemed btn1 = new GuiButtonThemed(bID + 0, 0, 0, btnWidth - 20, 20, I18n.format(quest.getTasks().getValue(tID.getID()).getUnlocalisedName()));
			GuiButtonThemed btn2 = new GuiButtonThemed(bID + 1, 0, 0, 20, 20, "" + TextFormatting.RED + TextFormatting.BOLD + "x");
			
			btnsLeft.addButtonRow(btn1, btn2);
		}
		
		for(int i = 0; i < taskTypes.size(); i++)
		{
			int btnWidth = btnsRight.getListWidth();
			int bID = (1 + i) << 2;
			
			GuiButtonThemed btn1 = new GuiButtonThemed(bID + 2, 0, 0, btnWidth, 20, taskTypes.get(i).getRegistryName().toString());
			
			btnsRight.addButtonRow(btn1);
		}
	}
}
