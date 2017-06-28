package adv_director.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import adv_director.api.network.IPacketHandler;
import adv_director.api.questing.IQuest;
import adv_director.network.PacketTypeNative;
import adv_director.questing.QuestDatabase;

public class PktHandlerDetect implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.DETECT.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		if(sender == null)
		{
			return;
		}
		
		IQuest quest = QuestDatabase.INSTANCE.getValue(data.getInteger("questID"));
		
		if(quest != null)
		{
			quest.detect(sender);
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
