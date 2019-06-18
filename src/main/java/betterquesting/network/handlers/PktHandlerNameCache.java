package betterquesting.network.handlers;

import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.QuestingPacket;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.NameCache;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PktHandlerNameCache implements IPacketHandler
{
    public static final PktHandlerNameCache INSTANCE = new PktHandlerNameCache();
    
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.NAME_CACHE.GetLocation();
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
		NameCache.INSTANCE.readFromNBT(tag.getTagList("data", 10), false);
	}
	
	public QuestingPacket getSyncPacket(@Nullable List<UUID> users)
    {
        NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", NameCache.INSTANCE.writeToNBT(new NBTTagList(), users));
		return new QuestingPacket(PacketTypeNative.NAME_CACHE.GetLocation(), tags);
    }
}
