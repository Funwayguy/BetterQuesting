package betterquesting.api.network;

import net.minecraft.util.ResourceLocation;

public interface IPacketRegistry
{
	void registerHandler(IPacketHandler handler);
	IPacketHandler getPacketHandler(ResourceLocation name);
}
