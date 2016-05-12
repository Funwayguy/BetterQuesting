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
		JsonObject json = new JsonObject();
		QuestDatabase.writeToJson(json);
		tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		PacketAssembly.SendTo(BQPacketType.QUEST_DATABASE.GetLocation(), tags, sender);
	}
	
	@Override
	public void handleClient(NBTTagCompound data) // Client side sync
	{
		JsonObject json = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Database"), new JsonObject());
		QuestDatabase.readFromJson(json);
	}
	
}
