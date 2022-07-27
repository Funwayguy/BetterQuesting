package betterquesting.network.handlers;

import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.utils.Tuple2;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.MinecraftForge;

public class NetNameSync {
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:name_sync");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetNameSync::onServer);

        if (BetterQuesting.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetNameSync::onClient);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void sendRequest(@Nullable UUID[] uuids, @Nullable String[] names) {
        // NOTE: You can make an empty request if you want EVERYTHING (but I would not recommend it on large servers)
        NBTTagCompound payload = new NBTTagCompound();
        if (uuids != null) {
            NBTTagList uList = new NBTTagList();
            for (UUID id : uuids) {
                if (id == null) continue;
                uList.appendTag(new NBTTagString(id.toString()));
            }
            payload.setTag("uuids", uList);
        }
        if (names != null) {
            NBTTagList nList = new NBTTagList();
            for (String s : names) {
                if (StringUtils.isNullOrEmpty(s)) continue;
                nList.appendTag(new NBTTagString(s));
            }
            payload.setTag("names", nList);
        }
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }

    public static void quickSync(@Nullable EntityPlayerMP player, int partyID) {
        IParty party = PartyManager.INSTANCE.getValue(partyID);
        if (party == null) return;

        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("data", NameCache.INSTANCE.writeToNBT(new NBTTagList(), party.getMembers()));
        payload.setBoolean("merge", true);

        if (player != null) {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
        } else {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            List<EntityPlayerMP> playerList = new ArrayList<>();
            for (UUID playerID : party.getMembers()) {
                EntityPlayerMP p = null;
                for (Object o : server.getConfigurationManager().playerEntityList) {
                    if (((EntityPlayerMP) o).getGameProfile().getId().equals(playerID)) {
                        p = (EntityPlayerMP) o;
                        break;
                    }
                }

                if (p != null) playerList.add(p);
            }
            PacketSender.INSTANCE.sendToPlayers(
                    new QuestingPacket(ID_NAME, payload), playerList.toArray(new EntityPlayerMP[0]));
        }
    }

    public static void sendNames(@Nullable EntityPlayerMP[] players, @Nullable UUID[] uuids, @Nullable String[] names) {
        List<UUID> idList = (uuids == null && names == null) ? null : new ArrayList<>();
        if (uuids != null) idList.addAll(Arrays.asList(uuids));
        if (names != null) {
            for (String s : names) {
                UUID id = NameCache.INSTANCE.getUUID(s);
                if (id != null) idList.add(id);
            }
        }

        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("data", NameCache.INSTANCE.writeToNBT(new NBTTagList(), idList));
        payload.setBoolean("merge", idList != null);

        if (players == null) {
            PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
        } else {
            PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), players);
        }
    }

    private static void onServer(Tuple2<NBTTagCompound, EntityPlayerMP> message) {
        UUID[] uuids = null;
        String[] names = null;

        if (message.getFirst().hasKey("uuids", 9)) {
            NBTTagList uList = message.getFirst().getTagList("uuids", 8);
            uuids = new UUID[uList.tagCount()];
            for (int i = 0; i < uuids.length; i++) {
                try {
                    uuids[i] = UUID.fromString(uList.getStringTagAt(i));
                } catch (Exception ignored) {
                }
            }
        }
        if (message.getFirst().hasKey("names", 9)) {
            NBTTagList uList = message.getFirst().getTagList("names", 8);
            names = new String[uList.tagCount()];
            for (int i = 0; i < names.length; i++) {
                names[i] = uList.getStringTagAt(i);
            }
        }
        sendNames(new EntityPlayerMP[] {message.getSecond()}, uuids, names);
    }

    @SideOnly(Side.CLIENT)
    private static void onClient(NBTTagCompound message) {
        NameCache.INSTANCE.readFromNBT(message.getTagList("data", 10), message.getBoolean("merge"));
        MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.NAMES));
    }
}
