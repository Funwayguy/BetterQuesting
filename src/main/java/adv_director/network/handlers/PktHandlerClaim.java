package adv_director.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import adv_director.api.api.QuestingAPI;
import adv_director.api.network.IPacketHandler;
import adv_director.api.questing.IQuest;
import adv_director.network.PacketTypeNative;
import adv_director.questing.QuestDatabase;

public class PktHandlerClaim implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.CLAIM.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		if(sender == null)
		{
			return;
		}
		
		IQuest quest = QuestDatabase.INSTANCE.getValue(data.getInteger("questID"));
		
		if(quest != null && !quest.hasClaimed(QuestingAPI.getQuestingUUID(sender)) && quest.canClaim(sender))
		{
			quest.claimReward(sender);
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
