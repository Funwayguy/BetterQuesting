package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestDatabase;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;

public class PktHandlerQuestDB extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data) // Sync request
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json1 = new JsonObject();
		QuestDatabase.writeToJson(json1);
		tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json1, new NBTTagCompound()));
		JsonObject json2 = new JsonObject();
		QuestDatabase.writeToJson_Progression(json2);
		tags.setTag("Progress", NBTConverter.JSONtoNBT_Object(json2, new NBTTagCompound()));
		PacketAssembly.SendTo(BQPacketType.QUEST_DATABASE.GetLocation(), tags, sender);
	}
	
	@Override
	public void handleClient(NBTTagCompound data) // Client side sync
	{
		JsonObject json1 = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Database"), new JsonObject());
		QuestDatabase.readFromJson(json1);
		JsonObject json2 = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Progress"), new JsonObject());
		QuestDatabase.readFromJson_Progression(json2);
	}
	
}
