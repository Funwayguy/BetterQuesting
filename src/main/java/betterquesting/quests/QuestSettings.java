package betterquesting.quests;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.database.IQuestSettings;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketSender;
import com.google.gson.JsonObject;

public class QuestSettings implements IQuestSettings
{
	public static final QuestSettings INSTANCE = new QuestSettings();
	
	private boolean editMode = true;
	private boolean hardcore = false;
	
	private QuestSettings()
	{
	}
	
	@Override
	public boolean isEditMode()
	{
		return editMode;
	}
	
	@Override
	public boolean isHardcore()
	{
		return hardcore;
	}
	
	@Override
	public void setEditMode(boolean state)
	{
		if(editMode == state)
		{
			return;
		}
		
		editMode = state;
		
		PacketSender.INSTANCE.sendToAll(getSyncPacket());
	}
	
	@Override
	public void setHardcore(boolean state)
	{
		if(hardcore == state)
		{
			return;
		}
		
		hardcore = state;
		
		PacketSender.INSTANCE.sendToAll(getSyncPacket());
	}
	
	@Override
	public PreparedPayload getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("settings", writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		return new PreparedPayload(PacketTypeNative.SETTINGS.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		
		readFromJson(JsonHelper.GetObject(base, "settings"), EnumSaveType.CONFIG);
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		json.addProperty("editMode", editMode);
		json.addProperty("hardcore", hardcore);
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		editMode = JsonHelper.GetBoolean(json, "editMode", true);
		hardcore = JsonHelper.GetBoolean(json, "hardcore", false);
	}
}
