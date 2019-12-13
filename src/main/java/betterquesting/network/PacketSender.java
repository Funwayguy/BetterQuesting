package betterquesting.network;

import betterquesting.api.network.IPacketSender;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api2.utils.BQThreadedIO;
import betterquesting.core.BetterQuesting;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;

import java.util.List;

public class PacketSender implements IPacketSender
{
	public static final PacketSender INSTANCE = new PacketSender();
	
	@Override
	public void sendToPlayers(QuestingPacket payload, ServerPlayerEntity... players)
	{
		payload.getPayload().putString("ID", payload.getHandler().toString());
        
        BQThreadedIO.INSTANCE.enqueue(() -> {
            List<CompoundNBT> fragments = PacketAssembly.INSTANCE.splitPacket(payload.getPayload());
            for(ServerPlayerEntity p : players)
            {
                for(CompoundNBT tag : fragments)
                {
                    BetterQuesting.instance.network.sendTo(new PacketQuesting(tag), p);
                }
            }
        });
	}
	
	@Override
	public void sendToAll(QuestingPacket payload)
	{
		payload.getPayload().putString("ID", payload.getHandler().toString());
		
		BQThreadedIO.INSTANCE.enqueue(() -> {
            for(CompoundNBT p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
            {
                BetterQuesting.instance.network.sendToAll(new PacketQuesting(p));
            }
        });
	}
	
	@Override
	public void sendToServer(QuestingPacket payload)
	{
		payload.getPayload().putString("ID", payload.getHandler().toString());
		
		BQThreadedIO.INSTANCE.enqueue(() -> {
            for(CompoundNBT p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
            {
                BetterQuesting.instance.network.sendToServer(new PacketQuesting(p));
            }
        });
	}
	
	@Override
	public void sendToAround(QuestingPacket payload, TargetPoint point)
	{
		payload.getPayload().putString("ID", payload.getHandler().toString());
		
		BQThreadedIO.INSTANCE.enqueue(() -> {
            for(CompoundNBT p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
            {
                BetterQuesting.instance.network.sendToAllAround(new PacketQuesting(p), point);
            }
        });
	}
	
	@Override
	public void sendToDimension(QuestingPacket payload, int dimension)
	{
		payload.getPayload().putString("ID", payload.getHandler().toString());
		
		BQThreadedIO.INSTANCE.enqueue(() -> {
            for(CompoundNBT p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
            {
                BetterQuesting.instance.network.sendToDimension(new PacketQuesting(p), dimension);
            }
        });
	}
}
