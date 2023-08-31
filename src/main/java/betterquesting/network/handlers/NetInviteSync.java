package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.party.PartyInvitations;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.UUID;

public class NetInviteSync {
  private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:invite_sync");

  public static void registerHandler() {
    if (BetterQuesting.proxy.isClient()) {
      PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetInviteSync::onClient);
    }
  }

  // If I need to send other people's invites to players then I'll deal with that another time
  public static void sendSync(@Nonnull EntityPlayerMP player) {
    NBTTagCompound payload = new NBTTagCompound();
    UUID playerID = QuestingAPI.getQuestingUUID(player);
    payload.setInteger("action", 0);
    payload.setTag("data", PartyInvitations.INSTANCE.writeToNBT(new NBTTagList(), Collections.singletonList(playerID)));
    PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
  }

  public static void sendRevoked(@Nonnull EntityPlayerMP player, int... IDs) {
    NBTTagCompound payload = new NBTTagCompound();
    payload.setInteger("action", 1);
    payload.setIntArray("IDs", IDs);
    PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
  }

  @SideOnly(Side.CLIENT)
  private static void onClient(NBTTagCompound message) {
    int action = message.getInteger("action");
    if (action == 0) {
      PartyInvitations.INSTANCE.readFromNBT(message.getTagList("data", 10), true);
      MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.PARTY));
    } else if (action == 1) {
      UUID playerID = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
      PartyInvitations.INSTANCE.revokeInvites(playerID, message.getIntArray("IDs"));
      MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.PARTY));
    }
  }
}
