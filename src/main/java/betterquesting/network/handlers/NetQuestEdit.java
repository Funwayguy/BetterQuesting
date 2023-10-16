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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.List;
import java.util.UUID;

public class NetQuestEdit {
  private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:quest_edit");

  public static void registerHandler() {
    PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetQuestEdit::onServer);

    if (BetterQuesting.proxy.isClient()) {
      PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetQuestEdit::onClient);
    }
  }

  @SideOnly(Side.CLIENT)
  public static void sendEdit(
      NBTTagCompound payload) // TODO: Make these use proper methods for each action rather than directly assembling the payload
  {
    PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
  }

  private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message) {
    EntityPlayerMP sender = message.getSecond();
    MinecraftServer server = sender.getServer();
    if (server == null) {
      return; // Here mostly just to keep intellisense happy
    }

    boolean isOP = server.getPlayerList().canSendCommands(sender.getGameProfile());

    if (!isOP) // OP pre-check
    {
      BetterQuesting.logger.log(Level.WARN,
                                "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) +
                                ") tried to edit quests without OP permissions!");
      sender.sendStatusMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit quests!"), true);
      return; // Player is not operator. Do nothing
    }

    NBTTagCompound tag = message.getFirst();
    UUID senderID = QuestingAPI.getQuestingUUID(sender);
    int action = !message.getFirst().hasKey("action", 99) ? -1 : message.getFirst().getInteger("action");

    switch (action) {
      case 0: {
        editQuests(tag.getTagList("data", 10));
        break;
      }
      case 1: {
        deleteQuests(tag.getIntArray("questIDs"));
        break;
      }
      case 2: {
        // TODO: Allow the editor to send a target player name/UUID
        setQuestStates(tag.getIntArray("questIDs"), tag.getBoolean("state"), senderID);
        break;
      }
      case 3: {
        createQuests(tag.getTagList("data", 10));
        break;
      }
      default: {
        BetterQuesting.logger.log(Level.ERROR, "Invalid quest edit action '" + action + "'. Full payload:\n" +
                                               message.getFirst());
      }
    }
  }

  // Serverside only
  public static void editQuests(NBTTagList data) {
    int[] ids = new int[data.tagCount()];
    for (int i = 0; i < data.tagCount(); i++) {
      NBTTagCompound entry = data.getCompoundTagAt(i);
      int questID = entry.getInteger("questID");
      ids[i] = questID;

      IQuest quest = QuestDatabase.INSTANCE.getValue(questID);
      if (quest != null) {
        quest.readFromNBT(entry.getCompoundTag("config"));
      }
    }

    SaveLoadHandler.INSTANCE.markDirty();
    NetQuestSync.sendSync(null, ids, true, false);
  }

  // Serverside only
  public static void deleteQuests(int[] questIDs) {
    for (int id : questIDs) {
      QuestDatabase.INSTANCE.removeID(id);
      QuestLineDatabase.INSTANCE.removeQuest(id);
    }

    SaveLoadHandler.INSTANCE.markDirty();

    NBTTagCompound payload = new NBTTagCompound();
    payload.setIntArray("questIDs", questIDs);
    payload.setInteger("action", 1);
    PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
  }

  // Serverside only
  public static void setQuestStates(int[] questIDs, boolean state, UUID targetID) {
    List<DBEntry<IQuest>> questList = QuestDatabase.INSTANCE.bulkLookup(questIDs);

    for (DBEntry<IQuest> entry : questList) {
      if (!state) {
        entry.getValue().resetUser(targetID, true);
        continue;
      }

      if (entry.getValue().isComplete(targetID)) {
        entry.getValue().setClaimed(targetID, 0);
      } else {
        entry.getValue().setComplete(targetID, 0);

        int done = 0;

        if (!entry.getValue().getProperty(NativeProps.LOGIC_TASK)
                  .getResult(done, entry.getValue().getTasks().size())) // Preliminary check
        {
          for (DBEntry<ITask> task : entry.getValue().getTasks().getEntries()) {
            task.getValue().setComplete(targetID);
            done++;

            if (entry.getValue().getProperty(NativeProps.LOGIC_TASK)
                     .getResult(done, entry.getValue().getTasks().size())) {
              break; // Only complete enough quests to claim the reward
            }
          }
        }
      }
    }

    SaveLoadHandler.INSTANCE.markDirty();

    MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
    if (server == null) {
      return;
    }
    EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(targetID);
    //noinspection ConstantConditions
    if (player == null) {
      return;
    }
    NetQuestSync.sendSync(player, questIDs, false, true);
  }

  // Serverside only
  public static void createQuests(NBTTagList data) {
    int[] ids = new int[data.tagCount()];
    for (int i = 0; i < data.tagCount(); i++) {
      NBTTagCompound entry = data.getCompoundTagAt(i);
      int questID = entry.hasKey("questID", 99) ? entry.getInteger("questID") : -1;
      if (questID < 0) {
        questID = QuestDatabase.INSTANCE.nextID();
      }
      ids[i] = questID;

      IQuest quest = QuestDatabase.INSTANCE.getValue(questID);
      if (quest == null) {
        quest = QuestDatabase.INSTANCE.createNew(questID);
      }
      if (entry.hasKey("config", 10)) {
        quest.readFromNBT(entry.getCompoundTag("config"));
      }
    }

    SaveLoadHandler.INSTANCE.markDirty();
    NetQuestSync.sendSync(null, ids, true, false);
  }

  @SideOnly(Side.CLIENT)
  private static void onClient(NBTTagCompound message) // Imparts edit specific changes
  {
    int action = !message.hasKey("action", 99) ? -1 : message.getInteger("action");

    if (action == 1) // Change to a switch statement when more actions are required
    {
      for (int id : message.getIntArray("questIDs")) {
        QuestDatabase.INSTANCE.removeID(id);
        QuestLineDatabase.INSTANCE.removeQuest(id);
      }

      MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.CHAPTER));
    }
  }
}
