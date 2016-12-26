package betterquesting.api.network;

import net.minecraft.util.ResourceLocation;

public interface IPacketRegistry
{
	public void registerHandler(IPacketHandler handler);
	public IPacketHandler getPacketHandler(ResourceLocation name);
}
