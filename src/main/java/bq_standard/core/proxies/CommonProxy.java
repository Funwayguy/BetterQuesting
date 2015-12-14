package bq_standard.core.proxies;

import bq_standard.rewards.loot.LootRegistry;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new LootRegistry());
	}

	public void registerThemes()
	{
	}
}
