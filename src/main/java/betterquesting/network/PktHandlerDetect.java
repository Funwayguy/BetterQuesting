package betterquesting.network;

import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class PktHandlerDetect extends PktHandler
{
	
	@Override
	public IMessage handleServer(EntityPlayer sender, NBTTagCompound data)
	{
		if(sender == null)
		{
			return null;
		}
		
		QuestInstance quest = QuestDatabase.getQuestByID(data.getInteger("questID"));
		
		if(quest != null)
		{
			quest.Detect(sender);
		}
		
		return null;
	}
	
	@Override
	public IMessage handleClient(NBTTagCompound data)
	{
		return null;
	}
	
}
