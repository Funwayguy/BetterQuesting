package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.*;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.importers.ImportedQuestLines;
import betterquesting.client.importers.ImportedQuests;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class NetImport {
  private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:import");

  public static void registerHandler() {
    PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetImport::onServer);
  }

  public static void sendImport(@Nonnull IQuestDatabase questDB, @Nonnull IQuestLineDatabase chapterDB) {
    NBTTagCompound payload = new NBTTagCompound();
    payload.setTag("quests", questDB.writeToNBT(new NBTTagList(), null));
    payload.setTag("chapters", chapterDB.writeToNBT(new NBTTagList(), null));
    PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
  }

  private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message) {
    EntityPlayerMP sender = message.getSecond();
    if (sender.getServer() == null) { return; }

    boolean isOP = sender.getServer().getPlayerList().canSendCommands(sender.getGameProfile());

    if (!isOP) {
      BetterQuesting.logger.log(Level.WARN,
                                "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) +
                                ") tried to import quests without OP permissions!");
      sender.sendStatusMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit quests!"),
                               false);
      return; // Player is not operator. Do nothing
    }

    ImportedQuests impQuestDB = new ImportedQuests();
    IQuestLineDatabase impQuestLineDB = new ImportedQuestLines();

    impQuestDB.readFromNBT(message.getFirst().getTagList("quests", 10), false);
    impQuestLineDB.readFromNBT(message.getFirst().getTagList("chapters", 10), false);

    BetterQuesting.logger.log(Level.INFO, "Importing " + impQuestDB.size() + " quest(s) and " + impQuestLineDB.size() +
                                          " quest line(s) from " + sender.getGameProfile().getName());

    HashMap<Integer, Integer> remapped = getRemappedIDs(impQuestDB.getEntries());

    for (DBEntry<IQuest> entry : impQuestDB.getEntries()) {
      int[] oldIDs = Arrays.copyOf(entry.getValue().getRequirements(), entry.getValue().getRequirements().length);

      for (int n = 0; n < oldIDs.length; n++) {
        if (remapped.containsKey(oldIDs[n])) {
          oldIDs[n] = remapped.get(oldIDs[n]);
        }
      }

      entry.getValue().setRequirements(oldIDs);

      QuestDatabase.INSTANCE.add(remapped.get(entry.getID()), entry.getValue());
    }

    for (DBEntry<IQuestLine> questLine : impQuestLineDB.getEntries()) {
      List<DBEntry<IQuestLineEntry>> pendingQLE = new ArrayList<>();

      for (DBEntry<IQuestLineEntry> qle : questLine.getValue().getEntries()) {
        pendingQLE.add(qle);
        questLine.getValue().removeID(qle.getID());
      }

      for (DBEntry<IQuestLineEntry> qle : pendingQLE) {
        if (!remapped.containsKey(qle.getID())) {
          BetterQuesting.logger.error("Failed to import quest into quest line. Unable to remap ID " + qle.getID());
          continue;
        }

        questLine.getValue().add(remapped.get(qle.getID()), qle.getValue());
      }

      QuestLineDatabase.INSTANCE.add(QuestLineDatabase.INSTANCE.nextID(), questLine.getValue());
    }

    SaveLoadHandler.INSTANCE.markDirty();
    NetQuestSync.quickSync(-1, true, true);
    NetChapterSync.sendSync(null, null);
  }

  /**
   * Takes a list of imported IDs and returns a remapping to unused IDs
   */
  private static HashMap<Integer, Integer> getRemappedIDs(List<DBEntry<IQuest>> idList) {
    int[] nextIDs = getNextIDs(idList.size());
    HashMap<Integer, Integer> remapped = new HashMap<>();

    for (int i = 0; i < nextIDs.length; i++) {
      remapped.put(idList.get(i).getID(), nextIDs[i]);
    }

    return remapped;
  }

  private static int[] getNextIDs(int num) {
    List<DBEntry<IQuest>> listDB = QuestDatabase.INSTANCE.getEntries();
    int[] nxtIDs = new int[num];

    if (listDB.isEmpty() || listDB.get(listDB.size() - 1).getID() == listDB.size() - 1) {
      for (int i = 0; i < num; i++) { nxtIDs[i] = listDB.size() + i; }
      return nxtIDs;
    }

    int n1 = 0;
    int n2 = 0;
    for (int i = 0; i < num; i++) {
      while (n2 < listDB.size() && listDB.get(n2).getID() == n1) {
        n1++;
        n2++;
      }

      nxtIDs[i] = n1++;
    }

    return nxtIDs;
  }
}
