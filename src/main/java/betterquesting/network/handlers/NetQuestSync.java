package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.BQThreadedIO;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestDatabase;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NetQuestSync
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:quest_sync");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetQuestSync::onServer);
        
        if(BetterQuesting.proxy.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetQuestSync::onClient);
        }
    }
    
    public static void quickSync(int questID, boolean config, boolean progress)
    {
        if(!config && !progress) return;
        
        int[] IDs = questID < 0 ? null : new int[]{questID};
        
        if(config) sendSync(null, IDs, true, false); // We're not sending progress in this pass.
        
        if(progress)
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server == null) return;
            
            for(ServerPlayerEntity player : server.getPlayerList().getPlayers())
            {
                sendSync(player, IDs, false, true); // Progression only this pass
            }
        }
    }
    
    public static void sendSync(@Nullable ServerPlayerEntity player, @Nullable int[] questIDs, boolean config, boolean progress)
    {
        if((!config && !progress) || (questIDs != null && questIDs.length <= 0)) return;
        
        // Offload this to another thread as it could take a while to build
        BQThreadedIO.INSTANCE.enqueue(() -> {
            ListNBT dataList = new ListNBT();
            final List<DBEntry<IQuest>> questSubset = questIDs == null ? QuestDatabase.INSTANCE.getEntries() : QuestDatabase.INSTANCE.bulkLookup(questIDs);
            final UUID playerID = player == null ? null : QuestingAPI.getQuestingUUID(player);
            
            for(DBEntry<IQuest> entry : questSubset)
            {
                CompoundNBT tag = new CompoundNBT();
                
                if(config) tag.put("config", entry.getValue().writeToNBT(new CompoundNBT()));
                if(progress) tag.put("progress", entry.getValue().writeProgressToNBT(new CompoundNBT(), Collections.singletonList(playerID)));
                tag.putInt("questID", entry.getID());
                dataList.add(tag);
            }
            
            CompoundNBT payload = new CompoundNBT();
            payload.putBoolean("merge", !config || questIDs != null);
            payload.put("data", dataList);
            
            if(player == null)
            {
                PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
            } else
            {
                PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
            }
        });
    }
    
    // Asks the server to send specific quest data over
    @OnlyIn(Dist.CLIENT)
    public static void requestSync(@Nullable int[] questIDs, boolean configs, boolean progress)
    {
        CompoundNBT payload = new CompoundNBT();
        if(questIDs != null) payload.putIntArray("requestIDs", questIDs);
        payload.putBoolean("getConfig", configs);
        payload.putBoolean("getProgress", progress);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple<CompoundNBT, ServerPlayerEntity> message)
    {
        CompoundNBT payload = message.getA();
        int[] reqIDs = !payload.contains("requestIDs") ? null : payload.getIntArray("requestIDs");
        sendSync(message.getB(), reqIDs, payload.getBoolean("getConfig"), payload.getBoolean("getProgress"));
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundNBT message)
    {
        ListNBT data = message.getList("data", 10);
        if(!message.getBoolean("merge")) QuestDatabase.INSTANCE.reset();
        
        for(int i = 0; i < data.size(); i++)
        {
            CompoundNBT tag = data.getCompound(i);
            if(!tag.contains("questID", 99)) continue;
            int questID = tag.getInt("questID");
            
            IQuest quest = QuestDatabase.INSTANCE.getValue(questID);
            
            if(tag.contains("config", 10))
            {
                if(quest == null) quest = QuestDatabase.INSTANCE.createNew(questID);
                quest.readFromNBT(tag.getCompound("config"));
            }
            
            if(tag.contains("progress", 10) && quest != null)
            {
                quest.readProgressFromNBT(tag.getCompound("progress"), true);
            }
        }
        
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.QUEST));
    }
}
