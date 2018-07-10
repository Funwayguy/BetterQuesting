package betterquesting.client.gui.editors.tasks;

import betterquesting.client.gui2.editors.nbt.GuiNbtEditor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.ICallback;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;

public class GuiTaskEditDefault extends GuiScreenThemed implements ICallback<NBTTagCompound>
{
	private final IQuest quest;
	private final int qID;
	private final int tID;
	private final NBTTagCompound json;
	private boolean isDone = false;
	
	public GuiTaskEditDefault(GuiScreen parent, IQuest quest, ITask task)
	{
		super(parent, task.getUnlocalisedName());
		this.quest = quest;
		this.qID = QuestDatabase.INSTANCE.getID(quest);
		this.tID = quest.getTasks().getID(task);
		this.json = task.writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG);
		this.isDone = false;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		if(!isDone)
		{
			this.isDone = true;
			ITask task = quest.getTasks().getValue(tID);
			
			if(task != null)
			{
				this.mc.displayGuiScreen(new GuiNbtEditor(this, json, this));
			} else
			{
				this.mc.displayGuiScreen(parent);
			}
		} else
		{
			this.mc.displayGuiScreen(parent);
		}
	}

	@Override
	public void setValue(NBTTagCompound value)
	{
		ITask task = quest.getTasks().getValue(tID);
		
		if(task != null)
		{
			task.readFromNBT(value, EnumSaveType.CONFIG);
			this.SendChanges();
		}
	}
	
	public void SendChanges()
	{
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("config", quest.writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG));
		base.setTag("progress", quest.writeToNBT(new NBTTagCompound(), EnumSaveType.PROGRESS));
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
		tags.setInteger("questID", qID);
		tags.setTag("data", base);
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}
}
