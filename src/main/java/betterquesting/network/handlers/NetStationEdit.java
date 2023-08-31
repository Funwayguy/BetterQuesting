package betterquesting.network.handlers;

import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSetupStation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetStationEdit {
  @SideOnly(Side.CLIENT)
  public static void setupStation(BlockPos pos, int questID, int taskID, byte windowsID) {
    BetterQuesting.instance.network.sendToServer(new PacketSetupStation(pos, questID, taskID, windowsID));
  }

  @SideOnly(Side.CLIENT)
  public static void resetStation(BlockPos pos, byte windowID) {
    BetterQuesting.instance.network.sendToServer(new PacketSetupStation(pos, -1, -1, windowID));
  }
}
