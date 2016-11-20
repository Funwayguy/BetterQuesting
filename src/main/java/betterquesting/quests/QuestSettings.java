package betterquesting.quests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.database.IQuestSettings;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.properties.NativeProps;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import com.google.gson.JsonObject;

public class QuestSettings extends PropertyContainer implements IQuestSettings
{
	public static final QuestSettings INSTANCE = new QuestSettings();
	
	private QuestSettings()
	{
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
	public boolean canUserEdit(EntityPlayer player)
	{
		if(player == null)
		{
			return false;
		}
		
		return this.getProperty(NativeProps.EDIT_MODE) && NameCache.INSTANCE.isOP(ExpansionAPI.getAPI().getNameCache().getQuestingID(player));
	}

	public void reset()
	{
		this.readFromJson(new JsonObject(), EnumSaveType.CONFIG);
	}
}
