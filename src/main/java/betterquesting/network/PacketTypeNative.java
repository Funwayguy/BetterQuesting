package betterquesting.network;

import net.minecraft.util.ResourceLocation;

// TODO: Move this to the API. Expansions need to make use of the edit and sync packets
public enum PacketTypeNative
{
	QUEST_DATABASE,
	PARTY_DATABASE,
	LINE_DATABASE,
	LIFE_DATABASE,
	QUEST_SYNC,
	QUEST_EDIT,
	PARTY_SYNC,
	PARTY_EDIT,
	LINE_SYNC,
	LINE_EDIT,
	DETECT,
	CLAIM,
	EDIT_STATION,
	NAME_CACHE,
	NOTIFICATION,
	SETTINGS,
	IMPORT,
    CACHE_SYNC,
    BULK;
	
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
