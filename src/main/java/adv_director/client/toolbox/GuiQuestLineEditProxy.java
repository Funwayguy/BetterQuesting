package adv_director.client.toolbox;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.api.ApiReference;
import adv_director.api.api.QuestingAPI;
import adv_director.api.client.gui.GuiScreenThemed;
import adv_director.api.enums.EnumPacketAction;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.misc.ICallback;
import adv_director.api.network.QuestingPacket;
import adv_director.api.questing.IQuestLine;
import adv_director.api.utils.NBTConverter;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import adv_director.questing.QuestLineDatabase;
import com.google.gson.JsonObject;

public class GuiQuestLineEditProxy extends GuiScreenThemed implements ICallback<JsonObject>
{
	private final IQuestLine line;
	private boolean flag = false;
	
	public GuiQuestLineEditProxy(GuiScreen parent, IQuestLine questLine)
	{
		super(parent, questLine.getUnlocalisedName());
		this.line = questLine;
	}
	
	public void initGui()
	{
		if(flag)
		{
			mc.displayGuiScreen(parent);
			return;
		} else
		{
			flag = true;
			QuestingAPI.getAPI(ApiReference.GUI_HELPER).openJsonEditor(this, this, line.writeToJson(new JsonObject(), EnumSaveType.CONFIG), null);
		}
	}
	
	public void SendChanges(EnumPacketAction action)
	{
		if(action == null)
		{
			return;
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		
		if(action == EnumPacketAction.EDIT && line != null)
		{
			JsonObject base = new JsonObject();
			base.add("line", line.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		}
		
		tags.setInteger("action", action.ordinal());
		tags.setInteger("lineID", QuestLineDatabase.INSTANCE.getKey(line));
		
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tags));
	}

	@Override
	public void setValue(JsonObject value)
	{
		line.readFromJson(value, EnumSaveType.CONFIG);
		SendChanges(EnumPacketAction.EDIT);
	}
}
