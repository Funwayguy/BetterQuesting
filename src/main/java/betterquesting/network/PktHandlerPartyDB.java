package betterquesting.network;

import betterquesting.network.PacketQuesting.PacketDataType;
import betterquesting.party.PartyManager;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class PktHandlerPartyDB extends PktHandler
{
	@Override
	public IMessage handleServer(EntityPlayer sender, NBTTagCompound data) // Sync request
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		PartyManager.writeToJson(json);
		tags.setTag("Parties", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		return PacketDataType.PARTY_DATABASE.makePacket(tags);
	}
	
	@Override
	public IMessage handleClient(NBTTagCompound data)
	{
		JsonObject json = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Parties"), new JsonObject());
		PartyManager.readFromJson(json);
		return null;
	}
	
}
