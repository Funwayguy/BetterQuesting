package betterquesting.network.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.IQuest;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class PktHandlerQuestSync implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.QUEST_SYNC.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		int questID = !data.hasKey("questID")? -1 : data.getInteger("questID");
		IQuest quest = QuestDatabase.INSTANCE.getValue(questID);
		
		if(quest == null) quest = QuestDatabase.INSTANCE.createNew(questID);
		
		quest.readPacket(data);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
}
