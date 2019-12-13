package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Level;

public class NetChapterEdit
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:chapter_edit");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetChapterEdit::onServer);
        
        if(BetterQuesting.proxy.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetChapterEdit::onClient);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void sendEdit(CompoundNBT payload) // TODO: Make these use proper methods for each action rather than directly assembling the payload
    {
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple<CompoundNBT, ServerPlayerEntity> message)
    {
        ServerPlayerEntity sender = message.getB();
        MinecraftServer server = sender.getServer();
        if(server == null) return; // Here mostly just to keep intellisense happy
        
        boolean isOP = server.getPlayerList().canSendCommands(sender.getGameProfile());
		
		if(!isOP) // OP pre-check
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit chapters without OP permissions!");
			sender.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "You need to be OP to edit quests!"), true);
			return; // Player is not operator. Do nothing
		}
		
		CompoundNBT tag = message.getA();
		int action = !message.getA().contains("action", 99) ? -1 : message.getA().getInt("action");
		
		switch(action)
        {
            case 0:
            {
                editChapters(tag.getList("data", 10));
                break;
            }
            case 1:
            {
                deleteChapters(tag.getIntArray("chapterIDs"));
                break;
            }
            case 2:
            {
                reorderChapters(tag.getIntArray("chapterIDs"));
                break;
            }
            case 3:
            {
                createChapters(tag.getList("data", 10));
                break;
            }
            default:
            {
                BetterQuesting.logger.log(Level.ERROR, "Invalid chapter edit action '" + action + "'. Full payload:\n" + message.getA().toString());
            }
        }
    }
    
    private static void editChapters(ListNBT data)
    {
        int[] ids = new int[data.size()];
        for(int i = 0; i < data.size(); i++)
        {
            CompoundNBT entry = data.getCompound(i);
            int chapterID = entry.getInt("chapterID");
            ids[i] = chapterID;
            
            IQuestLine chapter = QuestLineDatabase.INSTANCE.getValue(chapterID);
            if(chapter != null) chapter.readFromNBT(entry.getCompound("config"), false);
        }
    
        SaveLoadHandler.INSTANCE.markDirty();
        NetChapterSync.sendSync(null, ids);
    }
    
    private static void deleteChapters(int[] chapterIDs)
    {
        for(int id : chapterIDs)
        {
            QuestLineDatabase.INSTANCE.removeID(id);
        }
        
        SaveLoadHandler.INSTANCE.markDirty();
        
        CompoundNBT payload = new CompoundNBT();
        payload.putIntArray("chapterIDs", chapterIDs);
        payload.putInt("action", 1);
        PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void reorderChapters(int[] chapterIDs)
    {
        for(int n = 0; n < chapterIDs.length; n++)
        {
            QuestLineDatabase.INSTANCE.setOrderIndex(chapterIDs[n], n);
        }
        
        SaveLoadHandler.INSTANCE.markDirty();
        
        CompoundNBT payload = new CompoundNBT();
        payload.putIntArray("chapterIDs", chapterIDs);
        payload.putInt("action", 2);
        PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void createChapters(ListNBT data) // Includes future copy potential
    {
        int[] ids = new int[data.size()];
        for(int i = 0; i < data.size(); i++)
        {
            CompoundNBT entry = data.getCompound(i);
            int chapterID = entry.contains("chapterID", 99) ? entry.getInt("chapterID") : -1;
            if(chapterID < 0) chapterID = QuestLineDatabase.INSTANCE.nextID();
            ids[i] = chapterID;
            
            IQuestLine chapter = QuestLineDatabase.INSTANCE.getValue(chapterID);
            if(chapter == null) chapter = QuestLineDatabase.INSTANCE.createNew(chapterID);
            if(entry.contains("config", 10)) chapter.readFromNBT(entry.getCompound("config"), false);
        }
        
        SaveLoadHandler.INSTANCE.markDirty();
        NetChapterSync.sendSync(null, ids);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundNBT message)
    {
		int action = !message.contains("action", 99) ? -1 : message.getInt("action");
		
		switch(action) // Change to a switch statement when more actions are required
        {
            case 1: // Delete
            {
                for(int id : message.getIntArray("chapterIDs"))
                {
                    QuestLineDatabase.INSTANCE.removeID(id);
                }
        
		        MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.CHAPTER));
                break;
            }
            case 2: // Reorder
            {
                int[] chapterIDs = message.getIntArray("chapterIDs");
                for(int n = 0; n < chapterIDs.length; n++)
                {
                    QuestLineDatabase.INSTANCE.setOrderIndex(chapterIDs[n], n);
                }
                
		        MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.CHAPTER));
                break;
            }
        }
    }
}
