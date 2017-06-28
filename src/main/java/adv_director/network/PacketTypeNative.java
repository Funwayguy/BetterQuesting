package adv_director.network;

import net.minecraft.util.ResourceLocation;

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
	IMPORT;
	
	private final ResourceLocation ID;
	
	private PacketTypeNative()
	{
		this.ID = new ResourceLocation("betterquesting:" + this.toString().toLowerCase());
	}
	
	public ResourceLocation GetLocation()
	{
		return ID;
	}
}
