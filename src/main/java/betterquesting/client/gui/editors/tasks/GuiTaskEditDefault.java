package betterquesting.client.gui.editors.tasks;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.ICallback;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.NBTConverter;
import betterquesting.client.gui.editors.json.scrolling.GuiJsonEditor;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import com.google.gson.JsonObject;

public class GuiTaskEditDefault extends GuiScreenThemed implements ICallback<JsonObject>
{
	private final IQuest quest;
	private final ITask task;
	private final JsonObject json;
	private boolean isDone = false;
	
	public GuiTaskEditDefault(GuiScreen parent, IQuest quest, ITask task)
	{
		super(parent, task.getUnlocalisedName());
		this.quest = quest;
		this.task = task;
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
			this.mc.displayGuiScreen(new GuiJsonEditor(this, json, task.getDocumentation(), this));
		} else
		{
			this.mc.displayGuiScreen(parent);
		}
	}

	@Override
	public void setValue(JsonObject value)
	{
		task.readFromJson(value, EnumSaveType.CONFIG);
		this.SendChanges();
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
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}
}
