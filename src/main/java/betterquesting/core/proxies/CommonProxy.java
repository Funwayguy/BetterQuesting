package betterquesting.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import betterquesting.handlers.EventHandler;
import betterquesting.quests.tasks.advanced.AdvancedEventHandler;
import cpw.mods.fml.common.FMLCommonHandler;

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
	}

	public void registerThemes()
	{
	}
}
