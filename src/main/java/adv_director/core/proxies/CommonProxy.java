package adv_director.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import adv_director.api.api.IQuestExpansion;
import adv_director.core.AdvDirector;
import adv_director.core.ExpansionLoader;
import adv_director.handlers.EventHandler;
import adv_director.handlers.GuiHandler;

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
		
		NetworkRegistry.INSTANCE.registerGuiHandler(AdvDirector.instance, new GuiHandler());
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
