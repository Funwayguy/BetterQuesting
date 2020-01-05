package betterquesting.network;

import betterquesting.api.network.IPacketSender;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api2.utils.BQThreadedIO;
import betterquesting.core.BetterQuesting;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
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
                final PacketTarget pTarget = PacketDistributor.PLAYER.with(() -> p);
                for(CompoundNBT tag : fragments)
                {
                    BetterQuesting.instance.network.send(pTarget, new PacketQuesting(tag));
                }
            }
        });
	}
	
	@Override
	public void sendToAll(QuestingPacket payload)
	{
		payload.getPayload().putString("ID", payload.getHandler().toString());
        final PacketTarget pTarget = PacketDistributor.ALL.noArg();
		
		BQThreadedIO.INSTANCE.enqueue(() -> {
            for(CompoundNBT p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
            {
                BetterQuesting.instance.network.send(pTarget, new PacketQuesting(p));
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
                BetterQuesting.instance.network.send(PacketDistributor.SERVER.noArg(), new PacketQuesting(p));
                BetterQuesting.instance.network.sendToServer(new PacketQuesting(p));
            }
        });
	}
	
	@Override
	public void sendToAround(QuestingPacket payload, TargetPoint point)
	{
		payload.getPayload().putString("ID", payload.getHandler().toString());
		final PacketTarget pTarget = PacketDistributor.NEAR.with(() -> point);
		
		BQThreadedIO.INSTANCE.enqueue(() -> {
            for(CompoundNBT p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
            {
                BetterQuesting.instance.network.send(pTarget, new PacketQuesting(p));
            }
        });
	}
	
	@Override
	public void sendToDimension(QuestingPacket payload, DimensionType dimension)
	{
		payload.getPayload().putString("ID", payload.getHandler().toString());
		final PacketTarget pTarget = PacketDistributor.DIMENSION.with(() -> dimension);
		
		BQThreadedIO.INSTANCE.enqueue(() -> {
            for(CompoundNBT p : PacketAssembly.INSTANCE.splitPacket(payload.getPayload()))
            {
                BetterQuesting.instance.network.send(pTarget, new PacketQuesting(p));
            }
        });
	}
}
