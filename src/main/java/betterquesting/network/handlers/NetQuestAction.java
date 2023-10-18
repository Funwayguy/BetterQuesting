package betterquesting.network.handlers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.util.List;

public class NetQuestAction {
  private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:quest_action");

  public static void registerHandler() {
    PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetQuestAction::onServer);
  }

  @SideOnly(Side.CLIENT)
  public static void requestClaim(@Nonnull int[] questIDs) {
    if (questIDs.length == 0) {
      return;
    }
    NBTTagCompound payload = new NBTTagCompound();
    payload.setInteger("action", 0);
    payload.setIntArray("questIDs", questIDs);
    PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
  }

  @SideOnly(Side.CLIENT)
  public static void requestDetect(@Nonnull int[] questIDs) {
    if (questIDs.length == 0) {
      return;
    }
    NBTTagCompound payload = new NBTTagCompound();
    payload.setInteger("action", 1);
    payload.setIntArray("questIDs", questIDs);
    PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
  }

  private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message) {
    int action = !message.getFirst().hasKey("action", 99) ? -1 : message.getFirst().getInteger("action");

    switch (action) {
      case 0: {
        claimQuest(message.getFirst().getIntArray("questIDs"), message.getSecond());
        break;
      }
      case 1: {
        detectQuest(message.getFirst().getIntArray("questIDs"), message.getSecond());
        break;
      }
      default: {
        BetterQuesting.logger.log(Level.ERROR, "Invalid quest user action '" + action + "'. Full payload:\n" +
                                               message.getFirst());
      }
    }
  }

  public static void claimQuest(int[] questIDs, EntityPlayerMP player) {
    List<DBEntry<IQuest>> qLists = QuestDatabase.INSTANCE.bulkLookup(questIDs);

    for (DBEntry<IQuest> entry : qLists) {
      if (!entry.getValue().canClaim(player)) {
        continue;
      }
      entry.getValue().claimReward(player);
    }
  }

  public static void detectQuest(int[] questIDs, EntityPlayerMP player) {
    List<DBEntry<IQuest>> qLists = QuestDatabase.INSTANCE.bulkLookup(questIDs);

    for (DBEntry<IQuest> entry : qLists) {
      entry.getValue().detect(player);
    }
  }
}
