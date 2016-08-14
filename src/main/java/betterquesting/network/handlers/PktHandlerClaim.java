package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.quests.QuestDatabase;

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
		
		IQuestContainer quest = QuestDatabase.INSTANCE.getValue(data.getInteger("questID"));
		
		if(quest != null && !quest.hasClaimed(sender.getUniqueID()) && quest.canClaim(sender))
		{
			quest.claimReward(sender);
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
