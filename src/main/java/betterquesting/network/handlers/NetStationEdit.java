package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.blocks.TileSubmitStation;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetStationEdit
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:station_edit");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetStationEdit::onServer);
    }
    
    @SideOnly(Side.CLIENT)
    public static void sendEdit(NBTTagCompound tag)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("data", tag);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
    {
		NBTTagCompound tileData = message.getFirst().getCompoundTag("data");
		TileEntity tile = message.getSecond().world.getTileEntity(new BlockPos(tileData.getInteger("x"), tileData.getInteger("y"), tileData.getInteger("z")));
		if(tile instanceof TileSubmitStation)
        {
            TileSubmitStation ss = (TileSubmitStation)tile;
            if(ss.owner == null || ss.isUsableByPlayer(message.getSecond()))
            {
                ss.SyncTile(tileData);
            } else
            {
                BetterQuesting.logger.warn("Player " + message.getSecond().getName() + " attempted to hijack an OSS they do not own!");
            }
        }
    }
}
