package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestDatabase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PktHandlerQuestDB implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.QUEST_DATABASE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) // Sync request
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("Database", QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
		base.add("Progress", QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.PROGRESS));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		PacketSender.INSTANCE.sendToPlayer(PacketTypeNative.QUEST_DATABASE.GetLocation(), tags, sender);
	}
	
	@Override
	public void handleClient(NBTTagCompound data) // Client side sync
	{
		JsonObject base = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("data"), new JsonObject());
		JsonArray json1 = JsonHelper.GetArray(base, "Database");
		QuestDatabase.INSTANCE.readFromJson(json1, EnumSaveType.CONFIG);
		JsonArray json2 = JsonHelper.GetArray(base, "Progress");
		QuestDatabase.INSTANCE.readFromJson(json2, EnumSaveType.PROGRESS);
	}
}
