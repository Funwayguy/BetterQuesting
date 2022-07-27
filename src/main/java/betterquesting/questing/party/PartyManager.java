package betterquesting.questing.party;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class PartyManager extends SimpleDatabase<IParty> implements IPartyDatabase {
    public static final PartyManager INSTANCE = new PartyManager();

    public static void SyncPartyQuests(IParty party, UUID targetPlayer, boolean prohibitClaim) {
        ArrayList<UUID> uuids = new ArrayList<>();
        uuids.add(targetPlayer);
        SyncPartyQuests(party, uuids, prohibitClaim);
    }

    public static void SyncPartyQuests(IParty party, boolean prohibitClaim) {
        SyncPartyQuests(party, party.getMembers(), prohibitClaim);
    }

    private static void SyncPartyQuests(IParty party, List<UUID> targetUUIDs, boolean prohibitClaim) {
        new Thread(() -> {
                    BetterQuesting.logger.info("Start force party quest sync");
                    List<UUID> partyMembers = party.getMembers();

                    List<SyncPlayerContainer> t =
                            targetUUIDs.stream().map(SyncPlayerContainer::new).collect(Collectors.toList());

                    for (DBEntry<IQuest> questEntry : QuestDatabase.INSTANCE.getEntries()) {
                        IQuest quest = questEntry.getValue();
                        long completionTime = -1;
                        for (UUID member : partyMembers) {
                            NBTTagCompound completionInfo = quest.getCompletionInfo(member);
                            if (completionInfo != null) {
                                completionTime = completionInfo.getLong("timestamp");
                                break;
                            }
                        }

                        if (completionTime != -1) {
                            for (SyncPlayerContainer target : t) {
                                if (quest.isComplete(target.uuid)) continue;
                                quest.setComplete(target.uuid, completionTime);
                                if (prohibitClaim) {
                                    quest.setClaimed(target.uuid, completionTime);
                                }
                                if (target.isPlayerOnline()) {
                                    target.questCache.markQuestDirty(questEntry.getID());
                                }

                                target.questsCompleted += 1;
                            }
                        }
                    }

                    for (SyncPlayerContainer syncPlayerContainer : t) {
                        if (syncPlayerContainer.questsCompleted == 0) continue;
                        BetterQuesting.logger.info(
                                "Force party quest sync: completed " + syncPlayerContainer.questsCompleted
                                        + " quests for " + syncPlayerContainer.playerName);
                    }
                })
                .start();
    }

    private final HashMap<UUID, Integer> partyCache = new HashMap<>();

    @Override
    public synchronized IParty createNew(int id) {
        IParty party = new PartyInstance();
        if (id >= 0) this.add(id, party);
        return party;
    }

    @Nullable
    @Override
    public synchronized DBEntry<IParty> getParty(@Nonnull UUID uuid) {
        if (!QuestSettings.INSTANCE.getProperty(NativeProps.PARTY_ENABLE))
            return null; // We're merely preventing access. Not erasing data

        Integer cachedID = partyCache.get(uuid);
        IParty cachedParty = cachedID == null ? null : getValue(cachedID);

        if (cachedID != null && cachedParty == null) // Disbanded party
        {
            partyCache.remove(uuid);
        } else if (cachedParty != null) // Active party. Check validity...
        {
            EnumPartyStatus status = cachedParty.getStatus(uuid);
            if (status != null) return new DBEntry<>(cachedID, cachedParty);
            partyCache.remove(uuid); // User isn't a party member anymore
        }

        // NOTE: A server with a lot of solo players may still hammer this loop. Optimise further?
        for (DBEntry<IParty> entry : getEntries()) {
            EnumPartyStatus status = entry.getValue().getStatus(uuid);

            if (status != null) {
                partyCache.put(uuid, entry.getID());
                return entry;
            }
        }

        return null;
    }

    @Override
    public NBTTagList writeToNBT(NBTTagList json, @Nullable List<Integer> subset) {
        for (DBEntry<IParty> entry : getEntries()) {
            if (subset != null && !subset.contains(entry.getID())) continue;
            NBTTagCompound jp = entry.getValue().writeToNBT(new NBTTagCompound());
            jp.setInteger("partyID", entry.getID());
            json.appendTag(jp);
        }

        return json;
    }

    @Override
    public void readFromNBT(NBTTagList json, boolean merge) {
        if (!merge) reset();

        for (int i = 0; i < json.tagCount(); i++) {
            NBTTagCompound jp = json.getCompoundTagAt(i);

            int partyID = jp.hasKey("partyID", 99) ? jp.getInteger("partyID") : -1;
            if (partyID < 0) continue;

            IParty party = new PartyInstance();
            party.readFromNBT(jp);

            if (party.getMembers().size() > 0) {
                add(partyID, party);
            }
        }
    }

    @Override
    public synchronized void reset() {
        super.reset();
        partyCache.clear();
    }

    private static class SyncPlayerContainer {
        public SyncPlayerContainer(UUID uuid, EntityPlayerMP entityPlayerMP, QuestCache questCache, String playerName) {
            this.uuid = uuid;
            this.player = entityPlayerMP;
            this.questCache = questCache;
            this.playerName = playerName;
        }

        public SyncPlayerContainer(UUID uuid) {
            this.uuid = uuid;
            this.player = QuestingAPI.getPlayer(uuid);
            this.questCache = player != null
                    ? (QuestCache) player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString())
                    : null;
            this.playerName = player != null
                    ? player.getDisplayName()
                    : String.format("%s (%s)", uuid.toString(), NameCache.INSTANCE.getName(uuid));
        }

        public UUID uuid;
        public EntityPlayerMP player;
        public QuestCache questCache;
        public String playerName;
        public Integer questsCompleted = 0;

        public boolean isPlayerOnline() {
            return player != null;
        }
    }
}
