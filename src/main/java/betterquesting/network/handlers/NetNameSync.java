package betterquesting.network.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class NetNameSync
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:name_sync");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetNameSync::onServer);
        
        if(BetterQuesting.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetNameSync::onClient);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void sendRequest(@Nullable UUID[] uuids, @Nullable String[] names)
    {
        // NOTE: You can make an empty request if you want EVERYTHING (but I would not recommend it on large servers)
        CompoundNBT payload = new CompoundNBT();
        if(uuids != null)
        {
            ListNBT uList = new ListNBT();
            for(UUID id : uuids)
            {
                if(id == null) continue;
                uList.add(new StringNBT(id.toString()));
            }
            payload.put("uuids", uList);
        }
        if(names != null)
        {
            ListNBT nList = new ListNBT();
            for(String s : names)
            {
                if(StringUtils.isNullOrEmpty(s)) continue;
                nList.add(new StringNBT(s));
            }
            payload.put("names", nList);
        }
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    public static void quickSync(@Nullable ServerPlayerEntity player, int partyID)
    {
        IParty party = PartyManager.INSTANCE.getValue(partyID);
        if(party == null) return;
        
        CompoundNBT payload = new CompoundNBT();
        payload.put("data", NameCache.INSTANCE.writeToNBT(new ListNBT(), party.getMembers()));
        payload.putBoolean("merge", true);
        
        if(player != null)
        {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
        } else
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            List<ServerPlayerEntity> playerList = new ArrayList<>();
            for(UUID playerID : party.getMembers())
            {
                ServerPlayerEntity p = server.getPlayerList().getPlayerByUUID(playerID);
                //noinspection ConstantConditions
                if(p != null) playerList.add(p);
            }
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), playerList.toArray(new ServerPlayerEntity[0]));
        }
    }
    
    public static void sendNames(@Nullable ServerPlayerEntity[] players, @Nullable UUID[] uuids, @Nullable String[] names)
    {
        List<UUID> idList = (uuids == null && names == null) ? null : new ArrayList<>();
        if(uuids != null) idList.addAll(Arrays.asList(uuids));
        if(names != null)
        {
            for(String s : names)
            {
                UUID id = NameCache.INSTANCE.getUUID(s);
                if(id != null) idList.add(id);
            }
        }
        
        CompoundNBT payload = new CompoundNBT();
        payload.put("data", NameCache.INSTANCE.writeToNBT(new ListNBT(), idList));
        payload.putBoolean("merge", idList != null);
        
        if(players == null)
        {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        } else
        {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), players);
        }
    }
    
    private static void onServer(Tuple<CompoundNBT, ServerPlayerEntity> message)
    {
        UUID[] uuids = null;
        String[] names = null;
        
        if(message.getA().contains("uuids", 9))
        {
            ListNBT uList = message.getA().getList("uuids", 8);
            uuids = new UUID[uList.size()];
            for(int i = 0; i < uuids.length; i++)
            {
                try
                {
                    uuids[i] = UUID.fromString(uList.getString(i));
                } catch(Exception ignored){}
            }
        }
        if(message.getA().contains("names", 9))
        {
            ListNBT uList = message.getA().getList("names", 8);
            names = new String[uList.size()];
            for(int i = 0; i < names.length; i++)
            {
                names[i] =uList.getString(i);
            }
        }
        sendNames(new ServerPlayerEntity[]{message.getB()}, uuids, names);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundNBT message)
    {
        NameCache.INSTANCE.readFromNBT(message.getList("data", 10), message.getBoolean("merge"));
        MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.NAMES));
    }
}
