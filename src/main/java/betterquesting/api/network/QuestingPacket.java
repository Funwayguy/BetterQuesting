package betterquesting.api.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public final class QuestingPacket
{
	private final ResourceLocation handler;
	private final CompoundNBT payload;
	
	public QuestingPacket(ResourceLocation handler, CompoundNBT payload)
	{
		this.handler = handler;
		this.payload = payload;
	}
	
	public ResourceLocation getHandler()
	{
		return handler;
	}
	
	public CompoundNBT getPayload()
	{
		return payload;
	}
}
