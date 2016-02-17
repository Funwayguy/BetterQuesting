package betterquesting.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import betterquesting.client.UpdateNotification;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.EventHandler;
import betterquesting.handlers.GuiHandler;
import betterquesting.lives.LifeManager;
import betterquesting.quests.tasks.advanced.AdvancedEventHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		EventHandler handler = new EventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		MinecraftForge.TERRAIN_GEN_BUS.register(handler);
		FMLCommonHandler.instance().bus().register(handler);
		
		AdvancedEventHandler advHandle = new AdvancedEventHandler();
		MinecraftForge.EVENT_BUS.register(advHandle);
		MinecraftForge.TERRAIN_GEN_BUS.register(advHandle);
		FMLCommonHandler.instance().bus().register(advHandle);
		
		LifeManager lifeManager = new LifeManager();
		MinecraftForge.EVENT_BUS.register(lifeManager);
		FMLCommonHandler.instance().bus().register(lifeManager);
		
		FMLCommonHandler.instance().bus().register(new UpdateNotification());
		
		NetworkRegistry.INSTANCE.registerGuiHandler(BetterQuesting.instance, new GuiHandler());
	}

	public void registerThemes()
	{
	}
}
