package betterquesting.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import betterquesting.api.api.IQuestExpansion;
import betterquesting.client.UpdateNotification;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ExpansionLoader;
import betterquesting.handlers.EventHandler;
import betterquesting.handlers.GuiHandler;
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
		
		FMLCommonHandler.instance().bus().register(new UpdateNotification());
		
		NetworkRegistry.INSTANCE.registerGuiHandler(BetterQuesting.instance, new GuiHandler());
		
		ExpansionLoader.INSTANCE.initCommonAPIs();
	}
	
	public void registerExpansions()
	{
		for(IQuestExpansion exp : ExpansionLoader.INSTANCE.getAllExpansions())
		{
			exp.loadExpansion();
		}
	}
}
