package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;

public class PktHandlerQuestSync extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
		QuestInstance quest = QuestDatabase.getQuestByID(data.getInteger("questID"));
		
		if(quest != null)
		{
			NBTTagCompound tags = new NBTTagCompound();
			JsonObject json1 = new JsonObject();
			quest.writeToJSON(json1);
			tags.setInteger("questID", quest.questID);
			tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json1, new NBTTagCompound()));
			JsonObject json2 = new JsonObject();
			quest.writeProgressToJSON(json2);
			tags.setTag("Progress", NBTConverter.JSONtoNBT_Object(json2, new NBTTagCompound()));
			PacketAssembly.SendTo(BQPacketType.QUEST_SYNC.GetLocation(), tags, sender);
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		int questID = data.getInteger("questID");
		QuestInstance quest = QuestDatabase.getQuestByID(questID);
		quest = quest != null? quest : new QuestInstance(questID, false); // Server says this exists so create it
		
		JsonObject json1 = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Data"), new JsonObject());
		quest.readFromJSON(json1);
		JsonObject json2 = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Progress"), new JsonObject());
		quest.readProgressFromJSON(json2);
		
		QuestDatabase.updateUI = true; // Tell all UIs they need updating
	}
}
