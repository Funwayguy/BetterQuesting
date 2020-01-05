package betterquesting.core;

import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.CreativeTabQuesting;
import betterquesting.client.QuestNotification;
import betterquesting.commands.BQ_CommandAdmin;
import betterquesting.commands.BQ_CommandUser;
import betterquesting.handlers.ConfigHandler;
import betterquesting.handlers.EventHandler;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.PacketHandler;
import betterquesting.network.PacketQuesting;
import betterquesting.network.PacketTypeRegistry;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemGroup;
import net.minecraft.resources.FolderPackFinder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(BetterQuesting.MODID)
public class BetterQuesting
{
    public static final String MODID = "betterquesting";
    public static final String NAME = "BetterQuesting";
    public static final String CHANNEL = "bq_net_chan";
    public static final String NET_PROTOCOL = "1.0.0";
    public static final String FORMAT = "3.0.0";
    
    // TODO: Possibly make use of this in future
    private static final String MCL_API = "Yo1nkbXn7uVptLoL3GpkAaT7HsU8QFGJ";
	
	public static BetterQuesting instance;
	
	public SimpleChannel network;
	public static Logger logger = LogManager.getLogger(MODID);
	
	public static ItemGroup tabQuesting = new CreativeTabQuesting();
	
	//public static Item extraLife = new ItemExtraLife();
	//public static Item guideBook = new ItemGuideBook();
	
	//public static Block submitStation = new BlockSubmitStation();
	
	public BetterQuesting()
    {
        instance = this;
        
        //ModLoadingContext.get().registerConfig(Type.COMMON, ConfigHandler.commonSpec);
        ModLoadingContext.get().registerConfig(Type.CLIENT, ConfigHandler.clientSpec);
    
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupCommon);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverStart);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverStop);
        
        FMLJavaModLoadingContext.get().getModEventBus().register(ConfigHandler.class);
    }
    
    public void setupCommon(final FMLCommonSetupEvent event)
    {
    	network = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, CHANNEL), () -> NET_PROTOCOL, NET_PROTOCOL::equalsIgnoreCase, NET_PROTOCOL::equalsIgnoreCase);
    	network.registerMessage(0, PacketQuesting.class, PacketQuesting::toBytes, PacketQuesting::new, PacketHandler.INSTANCE);
    	
		MinecraftForge.EVENT_BUS.register(EventHandler.INSTANCE);
  
		ExpansionLoader.INSTANCE.initCommonAPIs();
    	PacketTypeRegistry.INSTANCE.initCommon();
    
        CapabilityProviderQuestCache.register();
    }
    
    public void setupClient(final FMLClientSetupEvent event)
    {
    	//PacketTypeRegistry.INSTANCE.initClient();
        
        // Figure this out
        /*if(!Minecraft.getInstance().getFramebuffer().isStencilEnabled())
		{
			if(!Minecraft.getInstance().getFramebuffer().enableStencil())
			{
				BetterQuesting.logger.error("[!] FAILED TO ENABLE STENCIL BUFFER. GUIS WILL BREAK! [!]");
			}
		}*/
		
		MinecraftForge.EVENT_BUS.register(PEventBroadcaster.INSTANCE);
		
		ExpansionLoader.INSTANCE.initClientAPIs();
		
		MinecraftForge.EVENT_BUS.register(new QuestNotification());
		BQ_Keybindings.RegisterKeys();
		
        Minecraft.getInstance().getResourcePackList().addPackFinder(new FolderPackFinder(new File("/config/betterquesting/resources/")));
        //Minecraft.getInstance().getResourceManager().addResourcePack(new QuestResourcesFile());
        //Minecraft.getInstance().getResourceManager().addResourcePack(new QuestResourcesFolder());
    }
    
    /*public void registerObjects(final FMLCommonSetupEvent event)
    {
    	FluidRegistry.registerFluid(FluidPlaceholder.fluidPlaceholder);
    	
    	GameRegistry.registerTileEntity(TileSubmitStation.class, new ResourceLocation(MODID + ":submit_station"));
    	
    	EntityRegistry.registerModEntity(new ResourceLocation(MODID + ":placeholder"), EntityPlaceholder.class, "placeholder", 0, this, 16, 1, false);
    }*/
	
	public void serverStart(final FMLServerStartingEvent event)
	{
		MinecraftServer server = event.getServer();
		CommandDispatcher<CommandSource> dispatch = server.getCommandManager().getDispatcher();
		
		BQ_CommandAdmin.register(dispatch);
		BQ_CommandUser.register(dispatch);
		//BQ_CommandDebug.register(dispatch);
		
		SaveLoadHandler.INSTANCE.loadDatabases(server);
	}
	
	public void serverStop(final FMLServerStoppedEvent event)
	{
		SaveLoadHandler.INSTANCE.unloadDatabases();
	}
	
	public static boolean isClient()
    {
        return FMLLoader.getDist() == Dist.CLIENT;
    }
}
