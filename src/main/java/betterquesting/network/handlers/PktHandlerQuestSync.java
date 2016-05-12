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
			JsonObject json = new JsonObject();
			quest.writeToJSON(json);
			tags.setInteger("questID", quest.questID);
			tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
			PacketAssembly.SendTo(BQPacketType.QUEST_SYNC.GetLocation(), tags, sender);
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		int questID = data.getInteger("questID");
		QuestInstance quest = QuestDatabase.getQuestByID(questID);
		quest = quest != null? quest : new QuestInstance(questID, false); // Server says this exists so create it
		
		JsonObject json = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Data"), new JsonObject());
		quest.readFromJSON(json);
		
		QuestDatabase.updateUI = true; // Tell all UIs they need updating
	}
	
}
