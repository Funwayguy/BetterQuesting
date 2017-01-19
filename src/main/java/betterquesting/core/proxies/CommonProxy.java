package betterquesting.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import betterquesting.api.api.IQuestExpansion;
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
		ExpansionLoader.INSTANCE.initCommonAPIs();
		
		EventHandler handler = new EventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		MinecraftForge.TERRAIN_GEN_BUS.register(handler);
		FMLCommonHandler.instance().bus().register(handler);
		
		NetworkRegistry.INSTANCE.registerGuiHandler(BetterQuesting.instance, new GuiHandler());
	}
	
	public void registerExpansions()
	{
		for(IQuestExpansion exp : ExpansionLoader.INSTANCE.getAllExpansions())
		{
			exp.loadExpansion();
		}
	}
}
