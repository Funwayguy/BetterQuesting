package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestDatabase;
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
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class NetQuestEdit
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:quest_edit");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetQuestEdit::onServer);
        
        if(BetterQuesting.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetQuestEdit::onClient);
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
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit quests without OP permissions!");
			sender.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "You need to be OP to edit quests!"), true);
			return; // Player is not operator. Do nothing
		}
		
		CompoundNBT tag = message.getA();
		UUID senderID = QuestingAPI.getQuestingUUID(sender);
		int action = !message.getA().contains("action", 99) ? -1 : message.getA().getInt("action");
		
		switch(action)
        {
            case 0:
            {
                editQuests(tag.getList("data", 10));
                break;
            }
            case 1:
            {
                deleteQuests(tag.getIntArray("questIDs"));
                break;
            }
            case 2:
            {
                // TODO: Allow the editor to send a target player name/UUID
                setQuestStates(tag.getIntArray("questIDs"), tag.getBoolean("state"), senderID);
                break;
            }
            case 3:
            {
                createQuests(tag.getList("data", 10));
                break;
            }
            default:
            {
                BetterQuesting.logger.log(Level.ERROR, "Invalid quest edit action '" + action + "'. Full payload:\n" + message.getA().toString());
            }
        }
    }
    
    // Serverside only
    public static void editQuests(ListNBT data)
    {
        int[] ids = new int[data.size()];
        for(int i = 0; i < data.size(); i++)
        {
            CompoundNBT entry = data.getCompound(i);
            int questID = entry.getInt("questID");
            ids[i] = questID;
            
            IQuest quest = QuestDatabase.INSTANCE.getValue(questID);
            if(quest != null) quest.readFromNBT(entry.getCompound("config"));
        }
    
        SaveLoadHandler.INSTANCE.markDirty();
        NetQuestSync.sendSync(null, ids, true, false);
    }
    
    // Serverside only
    public static void deleteQuests(int[] questIDs)
    {
        for(int id : questIDs)
        {
            QuestDatabase.INSTANCE.removeID(id);
            QuestLineDatabase.INSTANCE.removeQuest(id);
        }
        
        SaveLoadHandler.INSTANCE.markDirty();
        
        CompoundNBT payload = new CompoundNBT();
        payload.putIntArray("questIDs", questIDs);
        payload.putInt("action", 1);
        PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
    }
    
    // Serverside only
    public static void setQuestStates(@Nullable int[] questIDs, boolean state, UUID targetID)
    {
        List<DBEntry<IQuest>> questList = questIDs == null ? QuestDatabase.INSTANCE.getEntries() : QuestDatabase.INSTANCE.bulkLookup(questIDs);
        
        for(DBEntry<IQuest> entry : questList)
        {
            if(!state)
            {
                entry.getValue().resetUser(targetID, true);
                continue;
            }
            
            if(entry.getValue().isComplete(targetID))
            {
                entry.getValue().setClaimed(targetID, 0);
            } else
            {
                entry.getValue().setComplete(targetID, 0);
                
                int done = 0;
                
                if(!entry.getValue().getProperty(NativeProps.LOGIC_TASK).getResult(done, entry.getValue().getTasks().size())) // Preliminary check
                {
                    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
                    {
                        task.getValue().setComplete(targetID);
                        done++;
                        
                        if(entry.getValue().getProperty(NativeProps.LOGIC_TASK).getResult(done, entry.getValue().getTasks().size()))
                        {
                            break; // Only complete enough quests to claim the reward
                        }
                    }
                }
            }
        }
        
        SaveLoadHandler.INSTANCE.markDirty();
        
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server == null) return;
        ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(targetID);
        if(player == null) return;
        NetQuestSync.sendSync(player, questIDs, false, true);
    }
    
    // Serverside only
    public static void createQuests(ListNBT data)
    {
        int[] ids = new int[data.size()];
        for(int i = 0; i < data.size(); i++)
        {
            CompoundNBT entry = data.getCompound(i);
            int questID = entry.contains("questID", 99) ? entry.getInt("questID") : -1;
            if(questID < 0) questID = QuestDatabase.INSTANCE.nextID();
            ids[i] = questID;
            
            IQuest quest = QuestDatabase.INSTANCE.getValue(questID);
            if(quest == null) quest = QuestDatabase.INSTANCE.createNew(questID);
            if(entry.contains("config", 10)) quest.readFromNBT(entry.getCompound("config"));
        }
        
        SaveLoadHandler.INSTANCE.markDirty();
        NetQuestSync.sendSync(null, ids, true, false);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void onClient(CompoundNBT message) // Imparts edit specific changes
    {
		int action = !message.contains("action", 99) ? -1 : message.getInt("action");
		
		if(action == 1) // Change to a switch statement when more actions are required
        {
            for(int id : message.getIntArray("questIDs"))
            {
                QuestDatabase.INSTANCE.removeID(id);
                QuestLineDatabase.INSTANCE.removeQuest(id);
            }
        
		    MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.CHAPTER));
        }
    }
}
