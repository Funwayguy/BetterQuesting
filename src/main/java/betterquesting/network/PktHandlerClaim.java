package betterquesting.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;

public class PktHandlerClaim extends PktHandler
{
	
	@Override
	public IMessage handleServer(EntityPlayer sender, NBTTagCompound data)
	{
		if(sender == null)
		{
			return null;
		}
		
		QuestInstance quest = QuestDatabase.getQuestByID(data.getInteger("questID"));
		NBTTagList choiceData = data.getTagList("ChoiceData", 10);
		
		if(quest != null && !quest.HasClaimed(sender.getUniqueID()) && quest.CanClaim(sender, choiceData))
		{
			quest.Claim(sender, choiceData);
		}
		
		return null;
	}
	
	@Override
	public IMessage handleClient(NBTTagCompound data)
	{
		return null;
	}
	
}
