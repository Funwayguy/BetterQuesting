package betterquesting.network.handlers.quests;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
    
    @SideOnly(Side.CLIENT)
    public static void sendEdit(NBTTagCompound payload) // TODO: Make these use proper methods for each action rather than directly assembling the payload
    {
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
    {
        EntityPlayerMP sender = message.getSecond();
        MinecraftServer server = sender.getServer();
        if(server == null) return; // Here mostly just to keep intellisense happy
        
        boolean isOP = server.getPlayerList().canSendCommands(sender.getGameProfile());
		
		if(!isOP) // OP pre-check
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit chapters without OP permissions!");
			sender.sendStatusMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit quests!"), true);
			return; // Player is not operator. Do nothing
		}
		
		NBTTagCompound tag = message.getFirst();
		int action = !message.getFirst().hasKey("action", 99) ? -1 : message.getFirst().getInteger("action");
		
		switch(action)
        {
            case 0:
            {
                editChapters(tag.getTagList("data", 10));
                break;
            }
            case 1:
            {
                deleteChapters(tag.getIntArray("chapterIDs"));
                break;
            }
            case 2:
            {
                // TODO: Allow the editor to send a target player name/UUID
                reorderChapters(tag.getIntArray("chapterIDs"));
                break;
            }
            case 3:
            {
                createChapters(tag.getTagList("data", 10));
                break;
            }
            default:
            {
                BetterQuesting.logger.log(Level.ERROR, "Invalid chapter edit action '" + action + "'. Full payload:\n" + message.getFirst().toString());
            }
        }
    }
    
    private static void editChapters(NBTTagList data)
    {
        int[] ids = new int[data.tagCount()];
        for(int i = 0; i < data.tagCount(); i++)
        {
            NBTTagCompound entry = data.getCompoundTagAt(i);
            int questID = entry.getInteger("chapterID");
            ids[i] = questID;
            
            IQuestLine quest = QuestLineDatabase.INSTANCE.getValue(questID);
            if(quest != null) quest.readFromNBT(entry.getCompoundTag("config"));
        }
        
        NetChapterSync.sendSync(null, ids);
    }
    
    private static void reorderChapters(int[] chapterIDs)
    {
        for(int id : chapterIDs)
        {
            QuestLineDatabase.INSTANCE.removeID(id);
        }
        
        NBTTagCompound payload = new NBTTagCompound();
        payload.setIntArray("chapterIDs", chapterIDs);
        payload.setInteger("action", 1);
        PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void deleteChapters(int[] chapterIDs)
    {
        for(int n = 0; n < chapterIDs.length; n++)
        {
            QuestLineDatabase.INSTANCE.setOrderIndex(chapterIDs[n], n);
        }
        
        NBTTagCompound payload = new NBTTagCompound();
        payload.setIntArray("chapterIDs", chapterIDs);
        payload.setInteger("action", 2);
        PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void createChapters(NBTTagList data) // Includes future copy potential
    {
        int[] ids = new int[data.tagCount()];
        for(int i = 0; i < data.tagCount(); i++)
        {
            NBTTagCompound entry = data.getCompoundTagAt(i);
            int chapterID = entry.hasKey("chapterID", 99) ? entry.getInteger("chapterID") : -1;
            if(chapterID < 0) QuestLineDatabase.INSTANCE.nextID();
            ids[i] = chapterID;
            
            IQuestLine chapter = QuestLineDatabase.INSTANCE.getValue(chapterID);
            if(chapter == null) chapter = QuestLineDatabase.INSTANCE.getValue(chapterID);
            if(entry.hasKey("config", 10)) chapter.readFromNBT(entry.getCompoundTag("config"));
        }
        
        NetChapterSync.sendSync(null, ids);
    }
    
    @SideOnly(Side.CLIENT)
    private static void onClient(NBTTagCompound message)
    {
		int action = !message.hasKey("action", 99) ? -1 : message.getInteger("action");
		
		switch(action) // Change to a switch statement when more actions are required
        {
            case 1: // Delete
            {
                for(int id : message.getIntArray("chapterIDs"))
                {
                    QuestLineDatabase.INSTANCE.removeID(id);
                }
                break;
            }
            case 2: // Reorder
            {
                int[] chapterIDs = message.getIntArray("chapterIDs");
                for(int n = 0; n < chapterIDs.length; n++)
                {
                    QuestLineDatabase.INSTANCE.setOrderIndex(chapterIDs[n], n);
                }
                break;
            }
        }
    }
}
