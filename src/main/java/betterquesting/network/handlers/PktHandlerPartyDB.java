package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.party.PartyManager;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;

public class PktHandlerPartyDB extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data) // Sync request
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		PartyManager.writeToJson(json);
		tags.setTag("Parties", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		PacketAssembly.SendTo(BQPacketType.PARTY_DATABASE.GetLocation(), tags, sender);
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		JsonObject json = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Parties"), new JsonObject());
		PartyManager.readFromJson(json);
	}
	
}
