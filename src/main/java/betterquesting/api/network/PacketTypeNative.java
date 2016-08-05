package betterquesting.api.network;

import net.minecraft.util.ResourceLocation;

public enum PacketTypeNative
{
	QUEST_DATABASE,
	PARTY_DATABASE,
	QUEST_SYNC,
	QUEST_EDIT,
	LINE_EDIT,
	DETECT,
	CLAIM,
	PARTY_ACTION,
	LIFE_SYNC,
	EDIT_STATION,
	NOTIFICATION;
	
	public ResourceLocation GetLocation()
	{
		return new ResourceLocation("betterquesting:" + this.toString().toLowerCase());
	}
}
