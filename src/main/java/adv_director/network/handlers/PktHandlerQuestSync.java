package adv_director.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import adv_director.api.events.DatabaseEvent;
import adv_director.api.network.IPacketHandler;
import adv_director.api.questing.IQuest;
import adv_director.network.PacketTypeNative;
import adv_director.questing.QuestDatabase;
import adv_director.questing.QuestInstance;

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
		
		if(quest == null)
		{
			quest = new QuestInstance();
			QuestDatabase.INSTANCE.add(quest, questID);
		}
		
		quest.readPacket(data);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
}
