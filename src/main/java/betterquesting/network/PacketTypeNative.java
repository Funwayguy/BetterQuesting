package betterquesting.network;

import net.minecraft.util.ResourceLocation;

public enum PacketTypeNative
{
	PARTY_DATABASE,
	LIFE_DATABASE,
	PARTY_SYNC,
	PARTY_EDIT,
	DETECT,
	CLAIM,
	EDIT_STATION,
	NAME_CACHE,
	NOTIFICATION,
	SETTINGS,
	IMPORT,
    CACHE_SYNC
	;
	private final ResourceLocation ID;
	
	PacketTypeNative()
	{
		this.ID = new ResourceLocation("betterquesting:" + this.toString().toLowerCase());
	}
	
	public ResourceLocation GetLocation()
	{
		return ID;
	}
}
