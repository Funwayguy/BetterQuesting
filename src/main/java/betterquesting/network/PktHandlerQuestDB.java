package betterquesting.network;

import betterquesting.network.PacketQuesting.PacketDataType;
import betterquesting.quests.QuestDatabase;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class PktHandlerQuestDB extends PktHandler
{
	@Override
	public IMessage handleServer(EntityPlayer sender, NBTTagCompound data) // Sync request
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		QuestDatabase.writeToJson(json);
		tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		return PacketDataType.QUEST_DATABASE.makePacket(tags);
	}
	
	@Override
	public IMessage handleClient(NBTTagCompound data) // Client side sync
	{
		JsonObject json = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Database"), new JsonObject());
		QuestDatabase.readFromJson(json);
		return null;
	}
	
}
