package betterquesting.core;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.apache.logging.log4j.Logger;
import betterquesting.EntityPlaceholder;
import betterquesting.blocks.BlockSubmitStation;
import betterquesting.blocks.FluidPlaceholder;
import betterquesting.blocks.TileSubmitStation;
import betterquesting.client.CreativeTabQuesting;
import betterquesting.commands.BQ_Commands;
import betterquesting.commands.BQ_CommandsUser;
import betterquesting.core.proxies.CommonProxy;
import betterquesting.handlers.ConfigHandler;
import betterquesting.items.ItemExtraLife;
import betterquesting.items.ItemGuideBook;
import betterquesting.items.ItemPlaceholder;
import betterquesting.network.PacketQuesting;
import betterquesting.network.PacketTypeRegistry;
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
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = BetterQuesting.MODID, version = BetterQuesting.VERSION, name = BetterQuesting.NAME, guiFactory = "betterquesting.handlers.ConfigGuiFactory")
public class BetterQuesting
{
    public static final String MODID = "betterquesting";
    public static final String VERSION = "CI_MOD_VERSION";
    public static final String BRANCH = "CI_MOD_BRANCH";
    public static final String HASH = "CI_MOD_HASH";
    public static final String NAME = "BetterQuesting";
    public static final String PROXY = "betterquesting.core.proxies";
    public static final String CHANNEL = "BQ_NET_CHAN";
	
	@Instance(MODID)
	public static BetterQuesting instance;
	
	@SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
	public static CommonProxy proxy;
	public SimpleNetworkWrapper network;
	public static Logger logger;
	
	public static CreativeTabs tabQuesting = new CreativeTabQuesting();
	
	public static Item placeholder = new ItemPlaceholder();
	public static Item extraLife = new ItemExtraLife();
	public static Item guideBook = new ItemGuideBook();
	
	public static Block submitStation = new BlockSubmitStation();
	
	public static Fluid fluidPlaceholder = new FluidPlaceholder();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	logger = event.getModLog();
    	network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    	
    	ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile(), true);
    	ConfigHandler.initConfigs();
    	
    	proxy.registerHandlers();
    	PacketTypeRegistry.RegisterNativeHandlers();
    	
    	network.registerMessage(PacketQuesting.HandleClient.class, PacketQuesting.class, 0, Side.CLIENT);
    	network.registerMessage(PacketQuesting.HandleServer.class, PacketQuesting.class, 0, Side.SERVER);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	FluidRegistry.registerFluid(fluidPlaceholder);
    	
    	GameRegistry.registerItem(placeholder, "placeholder");
    	GameRegistry.registerItem(extraLife, "extra_life");
    	GameRegistry.registerItem(guideBook, "guide_book");
    	
    	GameRegistry.registerBlock(submitStation, "submit_station");
    	GameRegistry.registerTileEntity(TileSubmitStation.class, "submit_station");
    	
    	GameRegistry.addShapelessRecipe(new ItemStack(submitStation), new ItemStack(Items.book), new ItemStack(Blocks.glass), new ItemStack(Blocks.chest));
    	
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 0), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2));
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 0), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 1));
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 0), new ItemStack(extraLife, 1, 1), new ItemStack(extraLife, 1, 1));
    	
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 2, 1), new ItemStack(extraLife, 1, 0));
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 1), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2));
    	
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 2, 2), new ItemStack(extraLife, 1, 1));
    	
    	GameRegistry.addShapelessRecipe(new ItemStack(submitStation), new ItemStack(Items.book), new ItemStack(Blocks.chest), new ItemStack(Blocks.glass));
    	
    	EntityRegistry.registerModEntity(EntityPlaceholder.class, "placeholder", 0, this, 16, 1, false);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	proxy.registerThemes();
    }
	
	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{
		MinecraftServer server = MinecraftServer.getServer();
		ICommandManager command = server.getCommandManager();
		ServerCommandManager manager = (ServerCommandManager) command;
		
		manager.registerCommand(new BQ_Commands());
		manager.registerCommand(new BQ_CommandsUser());
	}
}
