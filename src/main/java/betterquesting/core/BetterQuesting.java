package betterquesting.core;

import betterquesting.api.placeholders.EntityPlaceholder;
import betterquesting.api.placeholders.FluidPlaceholder;
import betterquesting.api.placeholders.ItemPlaceholder;
import betterquesting.blocks.BlockObservationStation;
import betterquesting.blocks.BlockSubmitStation;
import betterquesting.blocks.TileObservationStation;
import betterquesting.blocks.TileSubmitStation;
import betterquesting.client.CreativeTabQuesting;
import betterquesting.commands.BQ_CommandAdmin;
import betterquesting.commands.BQ_CommandDebug;
import betterquesting.commands.BQ_CommandUser;
import betterquesting.commands.BQ_CopyProgress;
import betterquesting.core.proxies.CommonProxy;
import betterquesting.handlers.ConfigHandler;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.items.ItemExtraLife;
import betterquesting.items.ItemGuideBook;
import betterquesting.network.PacketQuesting;
import betterquesting.network.PacketTypeRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = BetterQuesting.MODID,
        name = BetterQuesting.NAME,
        version = BetterQuesting.VERSION,
        guiFactory = "betterquesting.handlers.ConfigGuiFactory")
public class BetterQuesting {
    public static final String MODID = "betterquesting";
    public static final String NAME = "BetterQuesting";
    public static final String VERSION = "GRADLETOKEN_VERSION";
    public static final String PROXY = "betterquesting.core.proxies";
    public static final String CHANNEL = "BQ_NET_CHAN";
    public static final String FORMAT = "2.0.0";

    @Instance(MODID)
    public static BetterQuesting instance;

    @SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
    public static CommonProxy proxy;

    public SimpleNetworkWrapper network;
    public static Logger logger;

    public static CreativeTabs tabQuesting = new CreativeTabQuesting();

    public static Item extraLife = new ItemExtraLife();
    public static Item guideBook = new ItemGuideBook();

    public static Block submitStation = new BlockSubmitStation();
    public static Block observationStation = new BlockObservationStation();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile(), true);
        ConfigHandler.initConfigs();

        proxy.registerHandlers();

        PacketTypeRegistry.INSTANCE.init();

        network.registerMessage(PacketQuesting.HandleClient.class, PacketQuesting.class, 0, Side.CLIENT);
        network.registerMessage(PacketQuesting.HandleServer.class, PacketQuesting.class, 0, Side.SERVER);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        FluidRegistry.registerFluid(FluidPlaceholder.fluidPlaceholder);

        GameRegistry.registerItem(ItemPlaceholder.placeholder, "placeholder");
        GameRegistry.registerItem(extraLife, "extra_life");
        GameRegistry.registerItem(guideBook, "guide_book");

        GameRegistry.registerBlock(submitStation, "submit_station");

        GameRegistry.registerTileEntity(TileSubmitStation.class, "submit_station");

        GameRegistry.registerBlock(observationStation, "observation_station");

        GameRegistry.registerTileEntity(TileObservationStation.class, "observation_station");
        if (!Loader.isModLoaded("dreamcraft")) {
            GameRegistry.addShapelessRecipe(
                    new ItemStack(submitStation),
                    new ItemStack(Items.book),
                    new ItemStack(Blocks.glass),
                    new ItemStack(Blocks.chest));
            GameRegistry.addShapelessRecipe(
                    new ItemStack(submitStation),
                    new ItemStack(Items.book),
                    new ItemStack(Blocks.chest),
                    new ItemStack(Blocks.glass));
            GameRegistry.addShapelessRecipe(
                    new ItemStack(observationStation), new ItemStack(submitStation), new ItemStack((Items.comparator)));
        }

        GameRegistry.addShapelessRecipe(
                new ItemStack(extraLife, 1, 0),
                new ItemStack(extraLife, 1, 2),
                new ItemStack(extraLife, 1, 2),
                new ItemStack(extraLife, 1, 2),
                new ItemStack(extraLife, 1, 2));
        GameRegistry.addShapelessRecipe(
                new ItemStack(extraLife, 1, 0),
                new ItemStack(extraLife, 1, 2),
                new ItemStack(extraLife, 1, 2),
                new ItemStack(extraLife, 1, 1));
        GameRegistry.addShapelessRecipe(
                new ItemStack(extraLife, 1, 0), new ItemStack(extraLife, 1, 1), new ItemStack(extraLife, 1, 1));

        GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 2, 1), new ItemStack(extraLife, 1, 0));
        GameRegistry.addShapelessRecipe(
                new ItemStack(extraLife, 1, 1), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2));

        GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 2, 2), new ItemStack(extraLife, 1, 1));

        EntityRegistry.registerModEntity(EntityPlaceholder.class, "placeholder", 0, this, 16, 1, false);

        proxy.registerRenderers();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {}

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        ICommandManager command = server.getCommandManager();
        ServerCommandManager manager = (ServerCommandManager) command;

        manager.registerCommand(new BQ_CopyProgress());
        manager.registerCommand(new BQ_CommandAdmin());
        manager.registerCommand(new BQ_CommandUser());

        if ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"))
            manager.registerCommand(new BQ_CommandDebug());

        SaveLoadHandler.INSTANCE.loadDatabases(server);
    }

    @EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
        SaveLoadHandler.INSTANCE.unloadDatabases();
    }
}
