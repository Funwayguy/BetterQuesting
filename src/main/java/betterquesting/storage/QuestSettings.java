package betterquesting.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.storage.IQuestSettings;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketTypeNative;
import com.google.gson.JsonObject;

public class QuestSettings extends PropertyContainer implements IQuestSettings
{
	public static final QuestSettings INSTANCE = new QuestSettings();
	
	private QuestSettings()
	{
		this.setupProps();
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("settings", writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.SETTINGS.GetLocation(), tags);
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
		
		return this.getProperty(NativeProps.EDIT_MODE) && NameCache.INSTANCE.isOP(QuestingAPI.getQuestingUUID(player));
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		super.readFromJson(json, saveType);
		
		this.setupProps();
	}
	
	public void reset()
	{
		this.readFromJson(new JsonObject(), EnumSaveType.CONFIG);
	}
	
	private void setupProps()
	{
		this.setupValue(NativeProps.EDIT_MODE);
		this.setupValue(NativeProps.HARDCORE);
		this.setupValue(NativeProps.LIVES_DEF);
		this.setupValue(NativeProps.LIVES_MAX);
		
		this.setupValue(NativeProps.HOME_IMAGE);
		this.setupValue(NativeProps.HOME_ANC_X);
		this.setupValue(NativeProps.HOME_ANC_Y);
		this.setupValue(NativeProps.HOME_OFF_X);
		this.setupValue(NativeProps.HOME_OFF_Y);
	}
	
	private <T> void setupValue(IPropertyType<T> prop)
	{
		this.setupValue(prop, prop.getDefault());
	}
	
	private <T> void setupValue(IPropertyType<T> prop, T def)
	{
		this.setProperty(prop, this.getProperty(prop, def));
	}
}
