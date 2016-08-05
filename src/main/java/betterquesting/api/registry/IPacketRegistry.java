package betterquesting.api.registry;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;

public interface IPacketRegistry
{
	public void registerHandler(IPacketHandler handler);
	public IPacketHandler getPacketHandler(ResourceLocation name);
}
