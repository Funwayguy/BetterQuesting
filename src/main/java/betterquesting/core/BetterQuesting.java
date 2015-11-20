package betterquesting.core;

import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;
import betterquesting.core.proxies.CommonProxy;
import betterquesting.handlers.ConfigHandler;
import betterquesting.items.ItemPlaceholder;
import betterquesting.network.PacketQuesting;
import betterquesting.quests.rewards.RewardChoice;
import betterquesting.quests.rewards.RewardItem;
import betterquesting.quests.rewards.RewardRegistry;
import betterquesting.quests.tasks.TaskCrafting;
import betterquesting.quests.tasks.TaskFluid;
import betterquesting.quests.tasks.TaskHunt;
import betterquesting.quests.tasks.TaskLocation;
import betterquesting.quests.tasks.TaskRegistry;
import betterquesting.quests.tasks.TaskRetrieval;
import betterquesting.quests.tasks.TaskScoreboard;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
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
    	
    	TaskRegistry.RegisterTask(TaskRetrieval.class, "retrieval");
    	TaskRegistry.RegisterTask(TaskHunt.class, "hunt");
    	TaskRegistry.RegisterTask(TaskLocation.class, "location");
    	TaskRegistry.RegisterTask(TaskCrafting.class, "crafting");
    	TaskRegistry.RegisterTask(TaskScoreboard.class, "scoreboard");
    	TaskRegistry.RegisterTask(TaskFluid.class, "fluid");
    	
    	RewardRegistry.RegisterReward(RewardItem.class, "item");
    	RewardRegistry.RegisterReward(RewardChoice.class, "choice");
    	
    	proxy.registerThemes();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
}
