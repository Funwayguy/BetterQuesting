package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestLineDatabase;

public class PktHandlerQuestLineDB implements IPacketHandler
{
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return null;
	}
	
	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
	{
		if(sender == null)
		{
			return;
		}
		
		PacketSender.INSTANCE.sendToPlayer(QuestLineDatabase.INSTANCE.getSyncPacket(), sender);
	}
	
	@Override
	public void handleClient(NBTTagCompound tag)
	{
		QuestLineDatabase.INSTANCE.readPacket(tag);
	}
}
