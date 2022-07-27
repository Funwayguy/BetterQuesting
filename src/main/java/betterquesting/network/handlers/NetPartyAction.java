package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.Tuple2;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.party.PartyInvitations;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Level;

public class NetPartyAction {
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:party_action");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetPartyAction::onServer);

        if (BetterQuesting.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetPartyAction::onClient);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void sendAction(NBTTagCompound payload) {
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }

    private static void onServer(Tuple2<NBTTagCompound, EntityPlayerMP> message) {
        EntityPlayerMP sender = message.getSecond();

        int action = !message.getFirst().hasKey("action", 99)
                ? -1
                : message.getFirst().getInteger("action");
        int partyID = !message.getFirst().hasKey("partyID", 99)
                ? -1
                : message.getFirst().getInteger("partyID");
        IParty party = PartyManager.INSTANCE.getValue(partyID);
        int permission = party == null ? 0 : checkPermission(QuestingAPI.getQuestingUUID(sender), party);

        switch (action) {
            case 0: {
                createParty(sender, message.getFirst().getString("name"));
                break;
            }
            case 1: {
                if (permission < 3) break;
                deleteParty(partyID);
                break;
            }
            case 2: {
                if (permission < 2) break;
                editParty(partyID, party, message.getFirst().getCompoundTag("data"));
                break;
            }
            case 3: {
                if (permission < 2) break;
                inviteUser(
                        partyID,
                        message.getFirst().getString("username"),
                        message.getFirst().getLong("expiry"));
                break;
            }
            case 4: {
                acceptInvite(partyID, sender); // Probably the only thing an OP can't force
                break;
            }
            case 5: {
                kickUser(partyID, sender, party, message.getFirst().getString("username"), permission);
                break;
            }
            default: {
                BetterQuesting.logger.log(
                        Level.ERROR,
                        "Invalid party action '" + action + "'. Full payload:\n"
                                + message.getFirst().toString());
            }
        }
    }

    private static void createParty(EntityPlayerMP sender, String name) {
        UUID playerID = QuestingAPI.getQuestingUUID(sender);
        if (PartyManager.INSTANCE.getParty(playerID) != null) return;

        int partyID = PartyManager.INSTANCE.nextID();
        IParty party = PartyManager.INSTANCE.createNew(partyID);
        party.getProperties().setProperty(NativeProps.NAME, name);
        party.setStatus(playerID, EnumPartyStatus.OWNER);
        NetPartySync.sendSync(new EntityPlayerMP[] {sender}, new int[] {partyID});
    }

    private static void deleteParty(int partyID) {
        PartyManager.INSTANCE.removeID(partyID);
        PartyInvitations.INSTANCE.purgeInvites(partyID);

        NBTTagCompound payload = new NBTTagCompound();
        payload.setInteger("action", 1);
        payload.setInteger("partyID", partyID);
        PacketSender.INSTANCE.sendToAll(
                new QuestingPacket(ID_NAME, payload)); // Invites need to be purged from everyone
    }

    private static void editParty(int partyID, IParty party, NBTTagCompound settings) {
        party.readProperties(settings);
        NetPartySync.quickSync(partyID);
    }

    private static void inviteUser(int partyID, String username, long expiry) {
        UUID uuid = null;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        EntityPlayerMP player = server.getConfigurationManager().func_152612_a(username);
        if (player != null) uuid = QuestingAPI.getQuestingUUID(player);
        if (uuid == null) uuid = NameCache.INSTANCE.getUUID(username);
        if (uuid != null) {
            PartyInvitations.INSTANCE.postInvite(uuid, partyID, expiry);
            if (player != null) {
                NetPartySync.sendSync(new EntityPlayerMP[] {player}, new int[] {partyID});
                NetInviteSync.sendSync(player);
            }
        } else {
            BetterQuesting.logger.error(
                    "Unable to identify " + username + " to invite to party " + partyID); // No idea who this is
        }
    }

    private static void acceptInvite(int partyID, EntityPlayerMP sender) {
        UUID playerID = QuestingAPI.getQuestingUUID(sender);
        DBEntry<IParty> party = PartyManager.INSTANCE.getParty(playerID);
        if (party != null) return;
        if (PartyInvitations.INSTANCE.acceptInvite(playerID, partyID)) {
            NetPartySync.quickSync(partyID);
            NetNameSync.quickSync(sender, partyID);
        } else {
            BetterQuesting.logger.error("Invalid invite for " + sender.getCommandSenderName() + " to party " + partyID);
        }
        NetInviteSync.sendSync(sender);
    }

    private static void kickUser(
            int partyID,
            EntityPlayerMP sender,
            IParty party,
            String username,
            int permission) // Is also the leave action (self kick if you will)
            {
        if (party == null) {
            BetterQuesting.logger.error("Tried to kick a player from a non-existant party (" + partyID + ")");
            return;
        }

        UUID uuid = null;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        EntityPlayerMP player = server.getConfigurationManager().func_152612_a(username);
        if (player != null) uuid = QuestingAPI.getQuestingUUID(player);
        if (uuid == null) uuid = NameCache.INSTANCE.getUUID(username);
        if (uuid == null) {
            BetterQuesting.logger.error("Unable to identify " + username + " to remove them from party " + partyID);
            return; // No idea who this is
        }

        if (uuid.equals(QuestingAPI.getQuestingUUID(sender))
                || checkPermission(uuid, party)
                        < permission) // For future reference, this is checking the target has a permission lower than
        // the sender
        {
            // Even if the kick isn't confirmed we still need to tell the clients incase of desync
            if (party.getStatus(uuid) != null) party.kickUser(uuid);

            if (party.getMembers().size() > 0) {
                NetPartySync.quickSync(partyID);
                if (player != null) {
                    NBTTagCompound payload = new NBTTagCompound();
                    payload.setInteger("action", 5);
                    payload.setInteger("partyID", partyID);
                    PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
                }
            } else // No more members. Delete the party
            {
                PartyManager.INSTANCE.removeID(partyID);
                PartyInvitations.INSTANCE.purgeInvites(partyID);

                NBTTagCompound payload = new NBTTagCompound();
                payload.setInteger("action", 1);
                payload.setInteger("partyID", partyID);
                PacketSender.INSTANCE.sendToAll(
                        new QuestingPacket(ID_NAME, payload)); // Invites need to be purged from everyone
            }
        } else {
            BetterQuesting.logger.error("Insufficient permissions to kick " + username + " from party " + partyID);
        }
    }

    private static int checkPermission(UUID playerID, IParty party) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            EntityPlayerMP player = null;
            for (Object o : server.getConfigurationManager().playerEntityList) {
                if (((EntityPlayerMP) o).getGameProfile().getId().equals(playerID)) {
                    player = (EntityPlayerMP) o;
                }
            }
            if (player != null && server.getConfigurationManager().func_152596_g(player.getGameProfile()))
                return 4; // Can kick owners or force invites without needing to be a member of the party
        }

        EnumPartyStatus status = party.getStatus(playerID);
        if (status == null) return 0; // Only OPs can edit parties they aren't a member of

        switch (status) {
            case MEMBER:
                return 1;
            case ADMIN:
                return 2;
            case OWNER:
                return 3;
            default:
                return 0;
        }
    }

    @SideOnly(Side.CLIENT)
    private static void onClient(NBTTagCompound message) {
        int action = !message.hasKey("action", 99) ? -1 : message.getInteger("action");
        int partyID = !message.hasKey("partyID", 99) ? -1 : message.getInteger("partyID");

        switch (action) {
            case 1: // Delete
            {
                PartyManager.INSTANCE.removeID(partyID);
                PartyInvitations.INSTANCE.purgeInvites(partyID);
                MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.PARTY));
                break;
            }
            case 5: // Kicked
            {
                IParty party = PartyManager.INSTANCE.getValue(partyID);
                if (party != null) {
                    party.kickUser(QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().thePlayer));
                    MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.PARTY));
                }
            }
        }
    }
}
