package betterquesting.network.handlers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NetStationEdit
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:station_edit");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetStationEdit::onServer);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void sendEdit(CompoundNBT tag)
    {
        CompoundNBT payload = new CompoundNBT();
        payload.put("data", tag);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple<CompoundNBT, ServerPlayerEntity> message)
    {
		CompoundNBT tileData = message.getA().getCompound("data");
		TileEntity tile = message.getB().world.getTileEntity(new BlockPos(tileData.getInt("x"), tileData.getInt("y"), tileData.getInt("z")));
		/*if(tile instanceof TileSubmitStation)
        {
            TileSubmitStation ss = (TileSubmitStation)tile;
            if(ss.owner == null || ss.isUsableByPlayer(message.getB()))
            {
                ss.SyncTile(tileData);
            } else
            {
                BetterQuesting.logger.warn("Player " + message.getB().getName() + " attempted to hijack an OSS they do not own!");
            }
        }*/
    }
}
