package betterquesting.network.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestLine;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

public class PktHandlerLineSync implements IPacketHandler
{
    public static final PktHandlerLineSync INSTANCE = new PktHandlerLineSync();
    
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.LINE_SYNC.GetLocation();
	}

	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
	{
		if(sender == null) return;
		
		int id = !tag.hasKey("lineID")? -1 : tag.getInteger("lineID");
		IQuestLine questLine = QuestLineDatabase.INSTANCE.getValue(id);
		
		if(questLine != null) PacketSender.INSTANCE.sendToPlayers(getSyncPacket(new DBEntry<>(id, questLine)), sender);
	}

	@Override
	public void handleClient(NBTTagCompound tag)
	{
		int id = !tag.hasKey("lineID")? -1 : tag.getInteger("lineID");
		IQuestLine questLine = QuestLineDatabase.INSTANCE.getValue(id);
		
		if(questLine == null)
		{
			questLine = new QuestLine();
			QuestLineDatabase.INSTANCE.add(id, questLine);
		}
		
		questLine.readFromNBT(tag.getCompoundTag("data").getCompoundTag("line"), true);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
	
	public QuestingPacket getSyncPacket(@Nonnull DBEntry<IQuestLine> line)
    {
        NBTTagCompound payload = new NBTTagCompound();
		payload.setTag("data", line.getValue().writeToNBT(new NBTTagCompound(), null));
		payload.setInteger("lineID", line.getID());
		
		return new QuestingPacket(PacketTypeNative.LINE_SYNC.GetLocation(), payload);
    }
}
