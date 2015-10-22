package betterquesting.core;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;
import betterquesting.core.proxies.CommonProxy;
import betterquesting.handlers.ConfigHandler;
import betterquesting.network.PacketQuesting;
import betterquesting.quests.rewards.*;
import betterquesting.quests.tasks.*;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
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
    	TaskRegistry.RegisterQuest(TaskRetrieval.class, "retrieval");
    	RewardRegistry.RegisterReward(RewardItem.class, "item");
    	RewardRegistry.RegisterReward(RewardChoice.class, "choice");
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
}
