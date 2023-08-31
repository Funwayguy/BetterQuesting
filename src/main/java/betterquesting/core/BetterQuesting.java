package betterquesting.core;

import betterquesting.api.placeholders.EntityPlaceholder;
import betterquesting.api.placeholders.FluidPlaceholder;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.blocks.BlockSubmitStation;
import betterquesting.blocks.TileSubmitStation;
import betterquesting.client.CreativeTabQuesting;
import betterquesting.commands.BQ_CommandAdmin;
import betterquesting.commands.BQ_CommandDebug;
import betterquesting.commands.BQ_CommandUser;
import betterquesting.core.proxies.CommonProxy;
import betterquesting.handlers.ConfigHandler;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.items.ItemExtraLife;
import betterquesting.items.ItemGuideBook;
import betterquesting.network.BetterQuestingPacketHandler;
import betterquesting.network.PacketTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = BetterQuesting.MODID,
    version = BetterQuesting.VERSION,
    name = BetterQuesting.NAME,
    guiFactory = "betterquesting.handlers.ConfigGuiFactory"
)
public class BetterQuesting {
  public static final String VERSION = "@VERSION@";
  public static final String MODID = "betterquesting";
  public static final String NAME = "BetterQuesting";
  public static final String FORMAT = "2.0.0";

  // TODO: Possibly make use of this in future
  private static final String MCL_API = "Yo1nkbXn7uVptLoL3GpkAaT7HsU8QFGJ";

  @Instance(MODID)
  public static BetterQuesting instance;

  @SidedProxy(clientSide = "betterquesting.core.proxies.ClientProxy",
              serverSide = "betterquesting.core.proxies.CommonProxy")
  public static CommonProxy proxy;
  public SimpleNetworkWrapper network;
  public static Logger logger;

  public static final CreativeTabs tabQuesting = new CreativeTabQuesting();

  public static final Item extraLife = new ItemExtraLife();
  public static final Item guideBook = new ItemGuideBook();

  public static final Block submitStation = new BlockSubmitStation();

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    logger = event.getModLog();
    network = NetworkRegistry.INSTANCE.newSimpleChannel("BQ_NET_CHAN");

    ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile(), true);
    ConfigHandler.initConfigs();

    proxy.registerHandlers();

    PacketTypeRegistry.INSTANCE.init();

    BetterQuestingPacketHandler.init();
    network = BetterQuestingPacketHandler.INSTANCE;

    CapabilityProviderQuestCache.register();
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    FluidRegistry.registerFluid(FluidPlaceholder.fluidPlaceholder);

    GameRegistry.registerTileEntity(TileSubmitStation.class, new ResourceLocation(MODID + ":submit_station"));

    EntityRegistry.registerModEntity(new ResourceLocation(MODID + ":placeholder"), EntityPlaceholder.class,
                                     "placeholder", 0, this, 16, 1, false);
  }

  @EventHandler
  public void serverStart(FMLServerStartingEvent event) {
    MinecraftServer server = event.getServer();
    ICommandManager command = server.getCommandManager();
    ServerCommandManager manager = (ServerCommandManager) command;

    manager.registerCommand(new BQ_CommandAdmin());
    manager.registerCommand(new BQ_CommandUser());

    if ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) {
      manager.registerCommand(new BQ_CommandDebug());
    }

    SaveLoadHandler.INSTANCE.loadDatabases(server);
  }

  @EventHandler
  public void serverStop(FMLServerStoppedEvent event) {
    SaveLoadHandler.INSTANCE.unloadDatabases();
  }
}
