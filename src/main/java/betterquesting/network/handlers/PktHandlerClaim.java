package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;

public class PktHandlerClaim extends PktHandler
{
	
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
		if(sender == null)
		{
			return;
		}
		
		QuestInstance quest = QuestDatabase.getQuestByID(data.getInteger("questID"));
		NBTTagList choiceData = data.getTagList("ChoiceData", 10);
		
		if(quest != null && !quest.HasClaimed(sender.getUniqueID()) && quest.CanClaim(sender, choiceData))
		{
			quest.Claim(sender, choiceData);
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
	
}
