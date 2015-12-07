package betterquesting.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.QuestNotification;

public class ClientProxy extends CommonProxy
{
	@Override
	public boolean isClient()
	{
		return true;
	}
	
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();
		MinecraftForge.EVENT_BUS.register(new QuestNotification());
		BQ_Keybindings.RegisterKeys();
	}
}
