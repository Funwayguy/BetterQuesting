package adv_director.client.gui.editors.tasks;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.client.gui.GuiScreenThemed;
import adv_director.api.enums.EnumPacketAction;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.misc.ICallback;
import adv_director.api.network.QuestingPacket;
import adv_director.api.questing.IQuest;
import adv_director.api.questing.tasks.ITask;
import adv_director.api.utils.NBTConverter;
import adv_director.client.gui.editors.json.scrolling.GuiJsonEditor;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import adv_director.questing.QuestDatabase;
import com.google.gson.JsonObject;

public class GuiTaskEditDefault extends GuiScreenThemed implements ICallback<JsonObject>
{
	private final IQuest quest;
	private final int qID;
	private final int tID;
	private final JsonObject json;
	private boolean isDone = false;
	
	public GuiTaskEditDefault(GuiScreen parent, IQuest quest, ITask task)
	{
		super(parent, task.getUnlocalisedName());
		this.quest = quest;
		this.qID = QuestDatabase.INSTANCE.getKey(quest);
		this.tID = quest.getTasks().getKey(task);
		this.json = task.writeToJson(new JsonObject(), EnumSaveType.CONFIG);
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
				this.mc.displayGuiScreen(new GuiJsonEditor(this, json, task.getDocumentation(), this));
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
	public void setValue(JsonObject value)
	{
		ITask task = quest.getTasks().getValue(tID);
		
		if(task != null)
		{
			task.readFromJson(value, EnumSaveType.CONFIG);
			this.SendChanges();
		}
	}
	
	public void SendChanges()
	{
		JsonObject base = new JsonObject();
		base.add("config", quest.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		base.add("progress", quest.writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
		tags.setInteger("questID", qID);
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}
}
