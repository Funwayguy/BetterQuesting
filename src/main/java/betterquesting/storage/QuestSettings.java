package betterquesting.storage;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.storage.IQuestSettings;
import betterquesting.network.PacketTypeNative;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class QuestSettings extends PropertyContainer implements IQuestSettings
{
	public static final QuestSettings INSTANCE = new QuestSettings();
	
	public QuestSettings()
	{
		this.setupProps();
	}
	
	@Override
    @Deprecated
	public QuestingPacket getSyncPacket()
	{
		return getSyncPacket(null);
	}
	
	@Override
	public QuestingPacket getSyncPacket(@Nullable List<UUID> users)
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", writeToNBT(new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.SETTINGS.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getCompoundTag("data"));
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
	public void readFromNBT(NBTTagCompound json)
	{
		super.readFromNBT(json);
		
		this.setupProps();
	}
	
	public void reset()
	{
		this.readFromNBT(new NBTTagCompound());
	}
	
	private void setupProps()
	{
		this.setupValue(NativeProps.PACK_NAME);
		this.setupValue(NativeProps.PACK_VER);
		
		this.setupValue(NativeProps.PARTY_ENABLE);
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
