package betterquesting.network.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.Tuple2;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.party.PartyManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

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
        
        if(BetterQuesting.proxy.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetPartySync::onClient);
        }
    }
    
    public static void quickSync(int partyID)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        IParty party = PartyManager.INSTANCE.getValue(partyID);
        
        if(server == null || party == null) return;
        
        List<EntityPlayerMP> players = new ArrayList<>();
        for(UUID uuid : party.getMembers())
        {
            EntityPlayerMP p = null;
            for(Object o : server.getConfigurationManager().playerEntityList)
            {
                if(((EntityPlayerMP)o).getGameProfile().getId().equals(uuid))
                {
                    p = (EntityPlayerMP)o;
                }
            }
            
            if(p != null) players.add(p);
        }
        
        sendSync(players.toArray(new EntityPlayerMP[0]), new int[]{partyID});
    }
    
    public static void sendSync(@Nullable EntityPlayerMP[] players, @Nullable int[] partyIDs)
    {
        if(partyIDs != null && partyIDs.length <= 0) return;
        if(players != null && players.length <= 0) return;
        
        NBTTagList dataList = new NBTTagList();
        final List<DBEntry<IParty>> partySubset = partyIDs == null ? PartyManager.INSTANCE.getEntries() : PartyManager.INSTANCE.bulkLookup(partyIDs);
        for(DBEntry<IParty> party : partySubset)
        {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setInteger("partyID", party.getID());
            entry.setTag("config", party.getValue().writeToNBT(new NBTTagCompound()));
            dataList.appendTag(entry);
        }
        
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("data", dataList);
        payload.setBoolean("merge", partyIDs != null);
        
        if(players == null)
        {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        } else
        {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), players);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public static void requestSync(@Nullable int[] partyIDs)
    {
        NBTTagCompound payload = new NBTTagCompound();
        if(partyIDs != null) payload.setIntArray("partyIDs", partyIDs);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple2<NBTTagCompound, EntityPlayerMP> message)
    {
        NBTTagCompound payload = message.getFirst();
        int[] reqIDs = !payload.hasKey("partyIDs") ? null : payload.getIntArray("partyIDs");
        sendSync(new EntityPlayerMP[]{message.getSecond()}, reqIDs);
    }
    
    @SideOnly(Side.CLIENT)
    private static void onClient(NBTTagCompound message)
    {
        NBTTagList data = message.getTagList("data", 10);
        if(!message.getBoolean("merge")) PartyManager.INSTANCE.reset();
        
        for(int i = 0; i < data.tagCount(); i++)
        {
            NBTTagCompound tag = data.getCompoundTagAt(i);
            if(!tag.hasKey("partyID", 99)) continue;
            int partyID = tag.getInteger("partyID");
            
            IParty party = PartyManager.INSTANCE.getValue(partyID); // TODO: Send to client side database
            if(party == null) party = PartyManager.INSTANCE.createNew(partyID);
            
            party.readFromNBT(tag.getCompoundTag("config"));
        }
        
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.PARTY));
    }
}
