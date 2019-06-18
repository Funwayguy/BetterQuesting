package betterquesting.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PktHandlerPartyDB implements IPacketHandler
{
    public static final PktHandlerPartyDB INSTANCE = new PktHandlerPartyDB();
    
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.PARTY_DATABASE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) // Sync request
	{
		if(sender == null) return;
		PacketSender.INSTANCE.sendToPlayers(getSyncPacket(QuestingAPI.getQuestingUUID(sender)), sender);
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		if(data.hasKey("data", 9)) PartyManager.INSTANCE.readFromNBT(data.getTagList("data", 10), data.getBoolean("merge"));
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
	
	public void resyncAll(boolean merge)
    {
	    MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        IPacketSender sender = QuestingAPI.getAPI(ApiReference.PACKET_SENDER);
        if(server == null || sender == null) return;
        
        // These would likely take some time to assemble so we'll queue them up on another thread
        for(EntityPlayerMP player : server.getPlayerList().getPlayers())
        {
            QuestingPacket packet = getSyncPacket(QuestingAPI.getQuestingUUID(player));
            if(!merge) packet.getPayload().setBoolean("merge", false);
            sender.sendToPlayers(packet, player);
        }
    }
    
    public void resyncPlayer(@Nonnull EntityPlayerMP player, boolean merge)
    {
        QuestingPacket packet = getSyncPacket(QuestingAPI.getQuestingUUID(player));
        if(!merge) packet.getPayload().setBoolean("merge", false);
        PacketSender.INSTANCE.sendToPlayers(packet, player);
    }
	
	public QuestingPacket getSyncPacket(@Nullable UUID user)
    {
        List<Integer> parties = null;
        
        if(user != null)
        {
            parties = PartyManager.INSTANCE.getPartyInvites(user);
            IParty curParty = PartyManager.INSTANCE.getUserParty(user);
            if(curParty != null) parties.add(PartyManager.INSTANCE.getID(curParty));
            PartyManager.INSTANCE.writeToNBT(new NBTTagList(), parties);
        }
        
        NBTTagCompound tags = new NBTTagCompound();
        tags.setBoolean("merge", user != null);
		tags.setTag("data", PartyManager.INSTANCE.writeToNBT(new NBTTagList(), parties));
		return new QuestingPacket(PacketTypeNative.PARTY_DATABASE.GetLocation(), tags);
    }
}
