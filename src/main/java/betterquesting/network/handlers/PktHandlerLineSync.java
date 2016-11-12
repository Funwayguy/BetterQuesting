package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.quests.IQuestLine;
import betterquesting.database.QuestLineDatabase;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestLine;

public class PktHandlerLineSync implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.LINE_SYNC.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
	{
		if(sender == null)
		{
			return;
		}
		
		int id = !tag.hasKey("lineID")? -1 : tag.getInteger("lineID");
		IQuestLine questLine = QuestLineDatabase.INSTANCE.getValue(id);
		
		if(questLine != null)
		{
			PacketSender.INSTANCE.sendToPlayer(questLine.getSyncPacket(), sender);
		}
	}

	@Override
	public void handleClient(NBTTagCompound tag)
	{
		int id = !tag.hasKey("lineID")? -1 : tag.getInteger("lineID");
		IQuestLine questLine = QuestLineDatabase.INSTANCE.getValue(id);
		
		if(questLine == null)
		{
			questLine = new QuestLine();
			QuestLineDatabase.INSTANCE.add(questLine, id);
		}
		
		questLine.readPacket(tag);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
}
