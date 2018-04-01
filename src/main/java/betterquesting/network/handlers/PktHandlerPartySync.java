package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.party.IParty;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyInstance;
import betterquesting.questing.party.PartyManager;

public class PktHandlerPartySync implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.PARTY_SYNC.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
	{
	}
	
	@Override
	public void handleClient(NBTTagCompound tag)
	{
		int partyID = !tag.hasKey("partyID")? -1 : tag.getInteger("partyID");
		
		if(partyID < 0)
		{
			return;
		}
		
		IParty party = PartyManager.INSTANCE.getValue(partyID);
		
		if(party == null)
		{
			party = new PartyInstance();
			PartyManager.INSTANCE.add(party, partyID);
		}
		
		party.readPacket(tag);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
}
