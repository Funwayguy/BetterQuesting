package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketSender;
import betterquesting.party.PartyManager;
import com.google.gson.JsonObject;

public class PktHandlerPartyDB implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.PARTY_DATABASE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) // Sync request
	{
		if(sender == null)
		{
			return;
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		PartyManager.writeToJson(json);
		tags.setTag("Parties", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		PacketSender.INSTANCE.sendToPlayer(PacketTypeNative.PARTY_DATABASE.GetLocation(), tags, sender);
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		JsonObject json = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Parties"), new JsonObject());
		PartyManager.readFromJson(json);
	}
}
