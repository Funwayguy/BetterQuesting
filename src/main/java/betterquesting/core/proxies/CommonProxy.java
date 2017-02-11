package betterquesting.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import betterquesting.api.api.IQuestExpansion;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ExpansionLoader;
import betterquesting.handlers.EventHandler;
import betterquesting.handlers.GuiHandler;

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
		
		NetworkRegistry.INSTANCE.registerGuiHandler(BetterQuesting.instance, new GuiHandler());
	}
	
	public void registerRenderers()
	{
	}
	
	public void registerExpansions()
	{
		for(IQuestExpansion exp : ExpansionLoader.INSTANCE.getAllExpansions())
		{
			exp.loadExpansion();
		}
	}
}
