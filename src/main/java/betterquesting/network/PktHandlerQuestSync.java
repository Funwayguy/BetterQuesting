package betterquesting.network;

import betterquesting.network.PacketQuesting.PacketDataType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class PktHandlerQuestSync extends PktHandler
{
	
	@Override
	public IMessage handleServer(EntityPlayer sender, NBTTagCompound data)
	{
		QuestInstance quest = QuestDatabase.getQuestByID(data.getInteger("questID"));
		
		if(quest != null)
		{
			NBTTagCompound tags = new NBTTagCompound();
			JsonObject json = new JsonObject();
			quest.writeToJSON(json);
			tags.setInteger("questID", quest.questID);
			tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
			return PacketDataType.QUEST_SYNC.makePacket(tags);
		}
		
		return null;
	}
	
	@Override
	public IMessage handleClient(NBTTagCompound data)
	{
		int questID = data.getInteger("questID");
		QuestInstance quest = QuestDatabase.getQuestByID(questID);
		quest = quest != null? quest : new QuestInstance(questID, false); // Server says this exists so create it
		
		JsonObject json = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Data"), new JsonObject());
		quest.readFromJSON(json);
		
		for(QuestLine line : QuestDatabase.questLines)
		{
			line.BuildTree(); // If a prerequisite change was made, trees need updating
		}
		
		QuestDatabase.updateUI = true; // Tell all UIs they need updating
		return null;
	}
	
}
