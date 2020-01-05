package betterquesting.network.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.party.PartyManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Ignore the invite system here. We'll deal wih that elsewhere
public class NetPartySync
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:party_sync");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetPartySync::onServer);
        
        if(BetterQuesting.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetPartySync::onClient);
        }
    }
    
    public static void quickSync(int partyID)
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        IParty party = PartyManager.INSTANCE.getValue(partyID);
        
        if(server == null || party == null) return;
        
        List<ServerPlayerEntity> players = new ArrayList<>();
        for(UUID uuid : party.getMembers())
        {
            ServerPlayerEntity p = server.getPlayerList().getPlayerByUUID(uuid);
            //noinspection ConstantConditions
            if(p != null) players.add(p);
        }
        
        sendSync(players.toArray(new ServerPlayerEntity[0]), new int[]{partyID});
    }
    
    public static void sendSync(@Nullable ServerPlayerEntity[] players, @Nullable int[] partyIDs)
    {
        if(partyIDs != null && partyIDs.length <= 0) return;
        if(players != null && players.length <= 0) return;
        
        ListNBT dataList = new ListNBT();
        final List<DBEntry<IParty>> partySubset = partyIDs == null ? PartyManager.INSTANCE.getEntries() : PartyManager.INSTANCE.bulkLookup(partyIDs);
        for(DBEntry<IParty> party : partySubset)
        {
            CompoundNBT entry = new CompoundNBT();
            entry.putInt("partyID", party.getID());
            entry.put("config", party.getValue().writeToNBT(new CompoundNBT()));
            dataList.add(entry);
        }
        
        CompoundNBT payload = new CompoundNBT();
        payload.put("data", dataList);
        payload.putBoolean("merge", partyIDs != null);
        
        if(players == null)
        {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        } else
        {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), players);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void requestSync(@Nullable int[] partyIDs)
    {
        CompoundNBT payload = new CompoundNBT();
        if(partyIDs != null) payload.putIntArray("partyIDs", partyIDs);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple<CompoundNBT, ServerPlayerEntity> message)
    {
        CompoundNBT payload = message.getA();
        int[] reqIDs = !payload.contains("partyIDs") ? null : payload.getIntArray("partyIDs");
        sendSync(new ServerPlayerEntity[]{message.getB()}, reqIDs);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundNBT message)
    {
        ListNBT data = message.getList("data", 10);
        if(!message.getBoolean("merge")) PartyManager.INSTANCE.reset();
        
        for(int i = 0; i < data.size(); i++)
        {
            CompoundNBT tag = data.getCompound(i);
            if(!tag.contains("partyID", 99)) continue;
            int partyID = tag.getInt("partyID");
            
            IParty party = PartyManager.INSTANCE.getValue(partyID); // TODO: Send to client side database
            if(party == null) party = PartyManager.INSTANCE.createNew(partyID);
            
            party.readFromNBT(tag.getCompound("config"));
        }
        
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.PARTY));
    }
}
