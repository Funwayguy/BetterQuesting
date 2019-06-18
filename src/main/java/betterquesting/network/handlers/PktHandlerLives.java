package betterquesting.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.network.QuestingPacket;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.LifeDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PktHandlerLives implements IPacketHandler
{
    public static final PktHandlerLives INSTANCE = new PktHandlerLives();
    
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.LIFE_DATABASE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleClient(NBTTagCompound data)
	{
		LifeDatabase.INSTANCE.readFromNBT(data.getCompoundTag("data").getCompoundTag("lives"), true);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
	
	public void resyncAll()
    {
	    MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        IPacketSender sender = QuestingAPI.getAPI(ApiReference.PACKET_SENDER);
        if(server == null || sender == null) return;
        
        // These would likely take some time to assemble so we'll queue them up on another thread
        for(EntityPlayerMP player : server.getPlayerList().getPlayers())
        {
            QuestingPacket packet = getSyncPacket(Collections.singletonList(QuestingAPI.getQuestingUUID(player)));
            sender.sendToPlayers(packet, player);
        }
    }
	
	public QuestingPacket getSyncPacket(@Nullable List<UUID> users)
    {
        NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("lives", LifeDatabase.INSTANCE.writeToNBT(new NBTTagCompound(), users));
		tags.setTag("data", base);
		
		return new QuestingPacket(PacketTypeNative.LIFE_DATABASE.GetLocation(), tags);
    }
}
