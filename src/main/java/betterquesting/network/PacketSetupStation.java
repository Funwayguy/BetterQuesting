package betterquesting.network;

import betterquesting.blocks.TileSubmitStation;
import betterquesting.client.gui2.inventory.ContainerSubmitStation;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetupStation implements IMessage {
  private BlockPos stationPos;
  private int questId;
  private int taskId;
  private byte sync;

  public PacketSetupStation() { }

  public PacketSetupStation(BlockPos stationPos, int questId, int taskId, byte sync) {
    this.stationPos = stationPos;
    this.questId = questId;
    this.taskId = taskId;
    this.sync = sync;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    stationPos = BlockPos.fromLong(buf.readLong());
    questId = buf.readInt();
    taskId = buf.readInt();
    sync = buf.readByte();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeLong(stationPos.toLong());
    buf.writeInt(questId);
    buf.writeInt(taskId);
    buf.writeByte(sync);
  }

  static class Handler implements IMessageHandler<PacketSetupStation, IMessage> {
    @Override
    public IMessage onMessage(PacketSetupStation message, MessageContext ctx) {
      EntityPlayerMP player = ctx.getServerHandler().player;
      WorldServer world = player.getServerWorld();
      BlockPos stationPos = message.stationPos;
      int questId = message.questId;
      int taskId = message.taskId;
      byte sync = message.sync;
      world.addScheduledTask(() -> {
        TileEntity tile = world.getTileEntity(stationPos);
        if (!(tile instanceof TileSubmitStation)) {
          return;
        }
        TileSubmitStation oss = (TileSubmitStation) tile;
        if (!oss.isUsableByPlayer(player)) {
          return;
        }
        Container container = player.openContainer;
        if (!(container instanceof ContainerSubmitStation)) {
          return;
        }
        ContainerSubmitStation containerSS = (ContainerSubmitStation) container;
        //If Vanilla checks windowId then there is some point in it, right?
        if (containerSS.tile != oss || containerSS.windowId != sync) {
          return;
        }
        oss.setupTask(player.getUniqueID(), questId, taskId);
      });
      return null;
    }
  }
}
