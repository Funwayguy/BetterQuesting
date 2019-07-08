package betterquesting.network.handlers.quests;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.BQThreadedIO;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    
    public static void sendSync(@Nullable EntityPlayerMP player, @Nullable int[] chapterIDs)
    {
        if(chapterIDs != null && chapterIDs.length <= 0) return;
        
        BQThreadedIO.INSTANCE.enqueue(() -> {
            NBTTagList data = new NBTTagList();
            final List<DBEntry<IQuestLine>> chapterSubset = chapterIDs == null ? QuestLineDatabase.INSTANCE.getEntries() : QuestLineDatabase.INSTANCE.bulkLookup(chapterIDs);
            
            for(DBEntry<IQuestLine> chapter : chapterSubset)
            {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setInteger("chapterID", chapter.getID());
                entry.setInteger("order", QuestLineDatabase.INSTANCE.getOrderIndex(chapter.getID()));
                entry.setTag("config", chapter.getValue().writeToNBT(new NBTTagCompound(), null));
                data.appendTag(entry);
            }
            
            NBTTagCompound payload = new NBTTagCompound();
            payload.setBoolean("merge", chapterIDs != null);
            payload.setTag("data", data);
            
            if(player == null)
            {
                PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
            } else
            {
                PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
            }
        });
    }
    
    @SideOnly(Side.CLIENT)
    public static void requestSync(@Nullable int[] chapterIDs)
    {
        NBTTagCompound payload = new NBTTagCompound();
        if(chapterIDs != null) payload.setIntArray("requestIDs", chapterIDs);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
    {
        NBTTagCompound payload = message.getFirst();
        int[] reqIDs = !payload.hasKey("requestIDs") ? null : payload.getIntArray("requestIDs");
        sendSync(message.getSecond(), reqIDs);
    }
    
    @SideOnly(Side.CLIENT)
    private static void onClient(NBTTagCompound message)
    {
        NBTTagList data = message.getTagList("data", 10);
        if(!message.getBoolean("merge")) QuestLineDatabase.INSTANCE.reset();
        
        for(int i = 0; i < data.tagCount(); i++)
        {
            NBTTagCompound tag = data.getCompoundTagAt(i);
            if(!tag.hasKey("chapterID", 99)) continue;
            int chapterID = tag.getInteger("chapterID");
            int order = tag.getInteger("order");
            
            IQuestLine chapter = QuestLineDatabase.INSTANCE.getValue(chapterID); // TODO: Send to client side database
            if(chapter == null) chapter = QuestLineDatabase.INSTANCE.createNew(chapterID);
            
            QuestLineDatabase.INSTANCE.setOrderIndex(chapterID, order);
            chapter.readFromNBT(tag.getCompoundTag("config"), false); // Merging isn't really a problem unless a chapter is excessively sized. Can be improved later if necessary
        }
        
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
    }
}
