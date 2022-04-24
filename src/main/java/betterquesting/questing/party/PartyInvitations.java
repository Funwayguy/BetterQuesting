package betterquesting.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.INBTPartial;
import betterquesting.core.BetterQuesting;
import betterquesting.network.handlers.NetInviteSync;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

// NOTE: This is in a separate class because it could later be moved to a dedicated inbox system
public class PartyInvitations implements INBTPartial<NBTTagList, UUID> {
    public static final PartyInvitations INSTANCE = new PartyInvitations();

    private final HashMap<UUID, HashMap<Integer, Long>> invites = new HashMap<>();

    public synchronized void postInvite(@Nonnull UUID uuid, int id, long expiryTime) {
        if (expiryTime <= 0) {
            BetterQuesting.logger.error("Received an invite that has already expired!");
            return; // Can't expire before being issued
        }

        IParty party = PartyManager.INSTANCE.getValue(id);
        if (party == null || party.getStatus(uuid) != null) return; // Party doesn't exist or user has already joined

        HashMap<Integer, Long> list = invites.computeIfAbsent(uuid, (key) -> new HashMap<>());
        list.put(id, System.currentTimeMillis() + expiryTime);
    }

    public synchronized boolean acceptInvite(@Nonnull UUID uuid, int id) {
        HashMap<Integer, Long> userInvites = invites.get(uuid);
        if (userInvites == null || userInvites.size() <= 0) return false;

        long timestamp = userInvites.get(id);
        IParty party = PartyManager.INSTANCE.getValue(id);
        boolean valid = timestamp > System.currentTimeMillis();

        if (valid && party != null) party.setStatus(uuid, EnumPartyStatus.MEMBER);

        userInvites.remove(id); // We still remove it regardless of validity
        if (userInvites.size() <= 0) invites.remove(uuid);

        return valid;
    }

    public synchronized void revokeInvites(@Nonnull UUID uuid, int... ids) {
        HashMap<Integer, Long> userInvites = invites.get(uuid);
        if (userInvites == null || userInvites.size() <= 0) return;
        for (int i : ids) userInvites.remove(i);
        if (userInvites.size() <= 0) invites.remove(uuid);
    }

    public synchronized List<Entry<Integer, Long>> getPartyInvites(@Nonnull UUID uuid) {
        HashMap<Integer, Long> userInvites = invites.get(uuid);
        if (userInvites == null || userInvites.size() <= 0) return Collections.emptyList();

        List<Entry<Integer, Long>> list = new ArrayList<>(userInvites.entrySet());
        list.sort(Comparator.comparing(Entry::getValue)); // Sort by expiry time
        return list;
    }

    // Primarily used when deleting parties to ensure that pending invites don't link to newly created parties under the same ID
    public synchronized void purgeInvites(int partyID) {
        invites.values().forEach((value) -> value.remove(partyID));
    }

    public synchronized void cleanExpired() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        Iterator<Entry<UUID, HashMap<Integer, Long>>> iterA = invites.entrySet().iterator();
        while (iterA.hasNext()) {
            Entry<UUID, HashMap<Integer, Long>> userInvites = iterA.next();

            List<Integer> revoked = new ArrayList<>();
            Iterator<Entry<Integer, Long>> iterB = userInvites.getValue().entrySet().iterator();
            while (iterB.hasNext()) {
                Entry<Integer, Long> entry = iterB.next();
                if (entry.getValue() < System.currentTimeMillis()) {
                    revoked.add(entry.getKey());
                    iterB.remove();
                }
            }
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(userInvites.getKey());
            //noinspection ConstantConditions
            if (player != null && revoked.size() >= 0) {
                int[] revAry = new int[revoked.size()];
                for (int i = 0; i < revoked.size(); i++) revAry[i] = revoked.get(i);
                NetInviteSync.sendRevoked(player, revAry); // Normally I avoid including networking calls into the database...
            }
            if (userInvites.getValue().size() <= 0) iterA.remove();
        }
    }

    public synchronized void reset() {
        invites.clear();
    }

    @Override
    public synchronized NBTTagList writeToNBT(NBTTagList nbt, @Nullable List<UUID> subset) // Don't bother saving this to disk. We do need to send packets though
    {
        if (subset != null) {
            subset.forEach((uuid) -> {
                NBTTagCompound userTag = new NBTTagCompound();
                userTag.setString("uuid", uuid.toString());

                Map<Integer, Long> userMap = invites.get(uuid);
                if (userMap == null) userMap = Collections.emptyMap();
                NBTTagList invList = new NBTTagList();

                for (Entry<Integer, Long> invEntry : userMap.entrySet()) {
                    NBTTagCompound invTag = new NBTTagCompound();
                    invTag.setInteger("partyID", invEntry.getKey());
                    invTag.setLong("expiry", invEntry.getValue());
                    invList.appendTag(invTag);
                }

                userTag.setTag("invites", invList);
                nbt.appendTag(userTag);
            });
        } else {
            for (Entry<UUID, HashMap<Integer, Long>> userMap : invites.entrySet()) {
                NBTTagCompound userTag = new NBTTagCompound();
                userTag.setString("uuid", userMap.getKey().toString());

                NBTTagList invList = new NBTTagList();
                for (Entry<Integer, Long> invEntry : userMap.getValue().entrySet()) {
                    NBTTagCompound invTag = new NBTTagCompound();
                    invTag.setInteger("partyID", invEntry.getKey());
                    invTag.setLong("expiry", invEntry.getValue());
                    invList.appendTag(invTag);
                }
                userTag.setTag("invites", invList);
                nbt.appendTag(userTag);
            }
        }
        return nbt;
    }

    @Override
    public synchronized void readFromNBT(NBTTagList nbt, boolean merge) {
        if (!merge) invites.clear();
        for (int i = 0; i < nbt.tagCount(); i++) {
            NBTTagCompound userEntry = nbt.getCompoundTagAt(i);
            UUID uuid;
            try {
                uuid = UUID.fromString(userEntry.getString("uuid"));
            } catch (Exception e) {
                continue;
            }

            NBTTagList invList = userEntry.getTagList("invites", 10);
            HashMap<Integer, Long> map = invites.compute(uuid, (key, old) -> new HashMap<>());
            map.clear();
            for (int n = 0; n < invList.tagCount(); n++) {
                NBTTagCompound invEntry = invList.getCompoundTagAt(n);
                int partyID = invEntry.hasKey("partyID", 99) ? invEntry.getInteger("partyID") : -1;
                long timestamp = invEntry.hasKey("expiry", 99) ? invEntry.getLong("expiry") : -1;
                if (partyID < 0) continue;
                map.put(partyID, timestamp);
            }
        }
    }
}
