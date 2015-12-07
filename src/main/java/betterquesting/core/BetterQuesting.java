package betterquesting.core;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;
import betterquesting.commands.BQ_Commands;
import betterquesting.core.proxies.CommonProxy;
import betterquesting.handlers.ConfigHandler;
import betterquesting.items.ItemPlaceholder;
import betterquesting.network.PacketQuesting;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = BetterQuesting.MODID, version = BetterQuesting.VERSION, name = BetterQuesting.NAME, guiFactory = "betterquesting.handlers.ConfigGuiFactory")
public class BetterQuesting
{
    public static final String MODID = "betterquesting";
    public static final String VERSION = "BQ_VER_KEY";
    public static final String NAME = "BetterQuesting";
    public static final String PROXY = "betterquesting.core.proxies";
    public static final String CHANNEL = "BQ_NET_CHAN";
	
	@Instance(MODID)
	public static BetterQuesting instance;
	
	@SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
	public static CommonProxy proxy;
	public SimpleNetworkWrapper network;
	public static Logger logger;
	
	public static Item placeholder = new ItemPlaceholder();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	logger = event.getModLog();
    	network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    	
    	ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile(), true);
    	ConfigHandler.initConfigs();
    	
    	proxy.registerHandlers();
    	
    	network.registerMessage(PacketQuesting.HandleClient.class, PacketQuesting.class, 0, Side.CLIENT);
    	network.registerMessage(PacketQuesting.HandleServer.class, PacketQuesting.class, 1, Side.SERVER);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	GameRegistry.registerItem(placeholder, "placeholder");
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
	
	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{
		MinecraftServer server = MinecraftServer.getServer();
		ICommandManager command = server.getCommandManager();
		ServerCommandManager manager = (ServerCommandManager) command;
		
		manager.registerCommand(new BQ_Commands());
	}
}
