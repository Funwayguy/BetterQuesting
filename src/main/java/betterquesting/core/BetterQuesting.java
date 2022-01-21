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
import betterquesting.commands.BQ_CopyProgress;
import betterquesting.commands.bqs.BQS_Commands;
import betterquesting.commands.bqs.BqsComDumpAdvancements;
import betterquesting.core.proxies.CommonProxy;
import betterquesting.handlers.ConfigHandler;
import betterquesting.handlers.LootSaveLoad;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.items.ItemExtraLife;
import betterquesting.items.ItemGuideBook;
import betterquesting.items.ItemLootChest;
import betterquesting.network.PacketQuesting;
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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = BetterQuesting.MODID, version = BetterQuesting.VERSION, name = BetterQuesting.NAME, guiFactory = "betterquesting.handlers.ConfigGuiFactory")
public class BetterQuesting
{
    public static final String VERSION = "@VERSION@";
    public static final String MODID = "betterquesting";
    public static final String NAME = "BetterQuesting";
    public static final String FORMAT = "2.0.0";

    // Used for some legacy compat
    public static final String MODID_STD = "bq_standard";

    public static boolean hasJEI = false;
    
    // TODO: Possibly make use of this in future
    private static final String MCL_API = "Yo1nkbXn7uVptLoL3GpkAaT7HsU8QFGJ";
	
	@Instance(MODID)
	public static BetterQuesting instance;
	
	@SidedProxy(clientSide = "betterquesting.core.proxies.ClientProxy", serverSide = "betterquesting.core.proxies.CommonProxy")
	public static CommonProxy proxy;
	public SimpleNetworkWrapper network;
	public static Logger logger;
	
	public static CreativeTabs tabQuesting = new CreativeTabQuesting();
	
	public static Item extraLife = new ItemExtraLife();
	public static Item guideBook = new ItemGuideBook();
    public static Item lootChest = new ItemLootChest();
	
	public static Block submitStation = new BlockSubmitStation();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	logger = event.getModLog();
    	network = NetworkRegistry.INSTANCE.newSimpleChannel("BQ_NET_CHAN");
    	
    	ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile(), true);
    	ConfigHandler.initConfigs();
    	
    	proxy.registerHandlers();
    	
    	PacketTypeRegistry.INSTANCE.init();
    	
    	network.registerMessage(PacketQuesting.HandleClient.class, PacketQuesting.class, 0, Side.CLIENT);
    	network.registerMessage(PacketQuesting.HandleServer.class, PacketQuesting.class, 0, Side.SERVER);
    
        CapabilityProviderQuestCache.register();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	FluidRegistry.registerFluid(FluidPlaceholder.fluidPlaceholder);
    	
    	GameRegistry.registerTileEntity(TileSubmitStation.class, new ResourceLocation(MODID + ":submit_station"));
    	
    	EntityRegistry.registerModEntity(new ResourceLocation(MODID + ":placeholder"), EntityPlaceholder.class, "placeholder", 0, this, 16, 1, false);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        hasJEI = Loader.isModLoaded("jei");
        proxy.registerExpansion();
    }
	
	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{
		MinecraftServer server = event.getServer();
		ICommandManager command = server.getCommandManager();
		ServerCommandManager manager = (ServerCommandManager) command;

		manager.registerCommand(new BQ_CommandAdmin());
		manager.registerCommand(new BQ_CommandUser());
		manager.registerCommand(new BQ_CopyProgress());
    
		manager.registerCommand(new BQS_Commands());
		manager.registerCommand(new BqsComDumpAdvancements());

		if((Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment")) manager.registerCommand(new BQ_CommandDebug());
		
		SaveLoadHandler.INSTANCE.loadDatabases(server);
        LootSaveLoad.INSTANCE.LoadLoot(event.getServer());
	}
	
	@EventHandler
	public void serverStop(FMLServerStoppedEvent event)
	{
		SaveLoadHandler.INSTANCE.unloadDatabases();
        LootSaveLoad.INSTANCE.UnloadLoot();
	}
}
