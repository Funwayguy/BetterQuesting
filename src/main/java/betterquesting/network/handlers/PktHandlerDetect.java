package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;

public class PktHandlerDetect extends PktHandler
{
	
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
		if(sender == null)
		{
			return;
		}
		
		QuestInstance quest = QuestDatabase.getQuestByID(data.getInteger("questID"));
		
		if(quest != null)
		{
			quest.Detect(sender);
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
	
}
