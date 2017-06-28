package adv_director.core;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import adv_director.api.placeholders.EntityPlaceholder;
import adv_director.api.placeholders.FluidPlaceholder;
import adv_director.api.placeholders.ItemPlaceholder;
import adv_director.blocks.BlockSubmitStation;
import adv_director.blocks.TileSubmitStation;
import adv_director.client.CreativeTabQuesting;
import adv_director.commands.BQ_CommandAdmin;
import adv_director.commands.BQ_CommandDebug;
import adv_director.commands.BQ_CommandUser;
import adv_director.core.proxies.CommonProxy;
import adv_director.handlers.ConfigHandler;
import adv_director.items.ItemExtraLife;
import adv_director.items.ItemGuideBook;
import adv_director.network.PacketQuesting;
import adv_director.network.PacketTypeRegistry;

@Mod(modid = AdvDirector.MODID, version = AdvDirector.VERSION, name = AdvDirector.NAME, guiFactory = AdvDirector.MODID + ".handlers.ConfigGuiFactory")
public class AdvDirector
{
    public static final String MODID = "adv_director";
    public static final String VERSION = "CI_MOD_VERSION";
    public static final String BRANCH = "CI_MOD_BRANCH";
    public static final String HASH = "CI_MOD_HASH";
    public static final String NAME = "Adventure Director";
    public static final String PROXY = MODID + ".core.proxies";
    public static final String CHANNEL = "ADV_DIR_CHAN";
    public static final String FORMAT = "2.0.0";
	
	@Instance(MODID)
	public static AdvDirector instance;
	
	@SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
	public static CommonProxy proxy;
	public SimpleNetworkWrapper network;
	public static Logger logger;
	
	public static CreativeTabs tabQuesting = new CreativeTabQuesting();
	
	public static Item extraLife = new ItemExtraLife();
	public static Item guideBook = new ItemGuideBook();
	
	public static Block submitStation = new BlockSubmitStation();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	logger = event.getModLog();
    	network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    	
    	ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile(), true);
    	ConfigHandler.initConfigs();
    	
    	proxy.registerHandlers();
    	
    	ExpansionLoader.INSTANCE.loadExpansions(event.getAsmData());
    	
    	if(PacketTypeRegistry.INSTANCE == null)
    	{
    		// Not actually required but for the sake of instantiating first...
    		AdvDirector.logger.log(Level.ERROR, "Unabled to instatiate packet registry");
    	}
    	
    	network.registerMessage(PacketQuesting.HandleClient.class, PacketQuesting.class, 0, Side.CLIENT);
    	network.registerMessage(PacketQuesting.HandleServer.class, PacketQuesting.class, 0, Side.SERVER);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	FluidRegistry.registerFluid(FluidPlaceholder.fluidPlaceholder);
    	
    	registerItem(ItemPlaceholder.placeholder, "placeholder");
    	registerItem(extraLife, "extra_life");
    	registerItem(guideBook, "guide_book");
    	
    	registerBlock(submitStation, "submit_station");
    	
    	GameRegistry.registerTileEntity(TileSubmitStation.class, "submit_station");
    	
    	GameRegistry.addShapelessRecipe(new ItemStack(submitStation), new ItemStack(Items.BOOK), new ItemStack(Blocks.GLASS), new ItemStack(Blocks.CHEST));
    	
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 0), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2));
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 0), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 1));
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 0), new ItemStack(extraLife, 1, 1), new ItemStack(extraLife, 1, 1));
    	
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 2, 1), new ItemStack(extraLife, 1, 0));
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 1, 1), new ItemStack(extraLife, 1, 2), new ItemStack(extraLife, 1, 2));
    	
    	GameRegistry.addShapelessRecipe(new ItemStack(extraLife, 2, 2), new ItemStack(extraLife, 1, 1));
    	
    	GameRegistry.addShapelessRecipe(new ItemStack(submitStation), new ItemStack(Items.BOOK), new ItemStack(Blocks.CHEST), new ItemStack(Blocks.GLASS));
    	
    	EntityRegistry.registerModEntity(EntityPlaceholder.class, "placeholder", 0, this, 16, 1, false);
    	
    	proxy.registerRenderers();
    }
    
    public void registerBlock(Block b, String name)
    {
    	ResourceLocation res = new ResourceLocation(MODID + ":" + name);
    	GameRegistry.register(b, res);
        GameRegistry.register(new ItemBlock(b).setRegistryName(res));
    }
    
    public void registerItem(Item i, String name)
    {
    	ResourceLocation res = new ResourceLocation(MODID + ":" + name);
        GameRegistry.register(i.setRegistryName(res));
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	proxy.registerExpansions();
    }
	
	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{
		MinecraftServer server = event.getServer();
		ICommandManager command = server.getCommandManager();
		ServerCommandManager manager = (ServerCommandManager) command;
		
		manager.registerCommand(new BQ_CommandAdmin());
		manager.registerCommand(new BQ_CommandUser());
		
		if(AdvDirector.VERSION == "CI_" + "MOD_VERSION")
		{
			manager.registerCommand(new BQ_CommandDebug());
		}
	}
}
