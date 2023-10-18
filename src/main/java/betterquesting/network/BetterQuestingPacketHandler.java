package betterquesting.network;

import betterquesting.core.BetterQuesting;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class BetterQuestingPacketHandler {
  public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(BetterQuesting.MODID);

  public static void init() {
    INSTANCE.registerMessage(PacketQuesting.HandleClient.class, PacketQuesting.class, 0, Side.CLIENT);
    INSTANCE.registerMessage(PacketQuesting.HandleServer.class, PacketQuesting.class, 0, Side.SERVER);
    INSTANCE.registerMessage(new PacketSetupStation.Handler(), PacketSetupStation.class, 1, Side.SERVER);
  }
}
