package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.IQuest;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.ResourceLocation;

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
		
		NBTBase tag = data.getTag("questID");
		
		if(tag instanceof NBTTagIntArray)
        {
            for(int id : ((NBTTagIntArray)tag).func_150302_c())
            {
                IQuest quest = QuestDatabase.INSTANCE.getValue(id);
        
                if(quest != null && !quest.hasClaimed(QuestingAPI.getQuestingUUID(sender)) && quest.canClaim(sender))
                {
                    quest.claimReward(sender);
                }
            }
        } else if(tag instanceof NBTTagInt)
        {
            IQuest quest = QuestDatabase.INSTANCE.getValue(data.getInteger("questID"));
    
            if(quest != null && !quest.hasClaimed(QuestingAPI.getQuestingUUID(sender)) && quest.canClaim(sender))
            {
                quest.claimReward(sender);
            }
        }
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
