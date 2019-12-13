package betterquesting.network.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.BQThreadedIO;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;

public class NetChapterSync
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:chapter_sync");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetChapterSync::onServer);
        
        if(BetterQuesting.proxy.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetChapterSync::onClient);
        }
    }
    
    public static void sendSync(@Nullable ServerPlayerEntity player, @Nullable int[] chapterIDs)
    {
        if(chapterIDs != null && chapterIDs.length <= 0) return;
        
        BQThreadedIO.INSTANCE.enqueue(() -> {
            ListNBT data = new ListNBT();
            final List<DBEntry<IQuestLine>> chapterSubset = chapterIDs == null ? QuestLineDatabase.INSTANCE.getEntries() : QuestLineDatabase.INSTANCE.bulkLookup(chapterIDs);
            
            for(DBEntry<IQuestLine> chapter : chapterSubset)
            {
                CompoundNBT entry = new CompoundNBT();
                entry.putInt("chapterID", chapter.getID());
                //entry.setInteger("order", QuestLineDatabase.INSTANCE.getOrderIndex(chapter.getID()));
                entry.put("config", chapter.getValue().writeToNBT(new CompoundNBT(), null));
                data.add(entry);
            }
            
            List<DBEntry<IQuestLine>> allSort = QuestLineDatabase.INSTANCE.getSortedEntries();
            int[] aryOrder = new int[allSort.size()];
            for(int i = 0; i < aryOrder.length; i++)
            {
                aryOrder[i] = allSort.get(i).getID();
            }
            
            CompoundNBT payload = new CompoundNBT();
            payload.putBoolean("merge", chapterIDs != null);
            payload.put("data", data);
            payload.putIntArray("order", aryOrder);
            
            if(player == null)
            {
                PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
            } else
            {
                PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
            }
        });
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void requestSync(@Nullable int[] chapterIDs)
    {
        CompoundNBT payload = new CompoundNBT();
        if(chapterIDs != null) payload.putIntArray("requestIDs", chapterIDs);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple<CompoundNBT, ServerPlayerEntity> message)
    {
        CompoundNBT payload = message.getA();
        int[] reqIDs = !payload.contains("requestIDs") ? null : payload.getIntArray("requestIDs");
        sendSync(message.getB(), reqIDs);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundNBT message)
    {
        ListNBT data = message.getList("data", 10);
        if(!message.getBoolean("merge")) QuestLineDatabase.INSTANCE.reset();
        
        for(int i = 0; i < data.size(); i++)
        {
            CompoundNBT tag = data.getCompound(i);
            if(!tag.contains("chapterID", 99)) continue;
            int chapterID = tag.getInt("chapterID");
            //int order = tag.getInteger("order");
            
            IQuestLine chapter = QuestLineDatabase.INSTANCE.getValue(chapterID); // TODO: Send to client side database
            if(chapter == null) chapter = QuestLineDatabase.INSTANCE.createNew(chapterID);
            
            //QuestLineDatabase.INSTANCE.setOrderIndex(chapterID, order);
            chapter.readFromNBT(tag.getCompound("config"), false); // Merging isn't really a problem unless a chapter is excessively sized. Can be improved later if necessary
        }
        
        int[] aryOrder = message.getIntArray("order");
        for(int i = 0; i < aryOrder.length; i++)
        {
            QuestLineDatabase.INSTANCE.setOrderIndex(aryOrder[i], i);
        }
        
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.CHAPTER));
    }
}
