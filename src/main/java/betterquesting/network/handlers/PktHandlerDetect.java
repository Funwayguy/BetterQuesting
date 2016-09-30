package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.quests.IQuest;
import betterquesting.quests.QuestDatabase;

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
