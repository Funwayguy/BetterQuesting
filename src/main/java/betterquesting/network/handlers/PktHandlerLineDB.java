package betterquesting.network.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.QuestingPacket;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;

public class PktHandlerLineDB implements IPacketHandler
{
    public static final PktHandlerLineDB INSTANCE = new PktHandlerLineDB();
    
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.LINE_DATABASE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
	{
		if(sender == null) return;
		PacketSender.INSTANCE.sendToPlayers(getSyncPacket(null), sender);
	}
	
	@Override
	public void handleClient(NBTTagCompound tag)
	{
		if(tag.hasKey("data", 9)) QuestLineDatabase.INSTANCE.readFromNBT(tag.getTagList("data", 10), tag.getBoolean("merge"));
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update()); // TODO: Improve this
	}
	
	public QuestingPacket getSyncPacket(@Nullable List<Integer> subset)
    {
        NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", QuestLineDatabase.INSTANCE.writeToNBT(new NBTTagList(), subset));
		tags.setBoolean("merge", subset != null);
		return new QuestingPacket(PacketTypeNative.LINE_DATABASE.GetLocation(), tags);
    }
}
