package betterquesting.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyInstance;
import betterquesting.questing.party.PartyManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PktHandlerPartySync implements IPacketHandler
{
    public static final PktHandlerPartySync INSTANCE = new PktHandlerPartySync();
    
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
		if(partyID < 0) return;
		
		IParty party = PartyManager.INSTANCE.getValue(partyID);
		
		if(party == null)
		{
			party = new PartyInstance();
			PartyManager.INSTANCE.add(partyID, party);
		}
		
		party.readFromNBT(tag.getCompoundTag("data"));
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
	
	public void syncParty(@Nonnull IParty party)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        IPacketSender sender = QuestingAPI.getAPI(ApiReference.PACKET_SENDER);
        if(server == null || sender == null) return;
        
        QuestingPacket packet = getSyncPacket(party);
        
        // These would likely take some time to assemble so we'll queue them up on another thread
        for(UUID memID : party.getMembers())
        {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(memID);
            if(player == null) continue;
            sender.sendToPlayers(packet, player);
        }
        
        for(UUID invite : party.getInvites())
        {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(invite);
            if(player == null) continue;
            sender.sendToPlayers(packet, player);
        }
    }
	
	private QuestingPacket getSyncPacket(@Nonnull IParty party)
    {
        NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", party.writeToNBT(new NBTTagCompound()));
		tags.setInteger("partyID", PartyManager.INSTANCE.getID(party));
		
		return new QuestingPacket(PacketTypeNative.PARTY_SYNC.GetLocation(), tags);
    }
}
