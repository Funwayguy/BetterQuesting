package betterquesting.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.storage.QuestSettings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PartyManager extends SimpleDatabase<IParty> implements IPartyDatabase {
    public static final PartyManager INSTANCE = new PartyManager();

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
    public synchronized NBTTagList writeToNBT(NBTTagList json, List<Integer> subset) {
        for (DBEntry<IParty> entry : getEntries()) {
            if (subset != null && !subset.contains(entry.getID())) continue;
            NBTTagCompound jp = entry.getValue().writeToNBT(new NBTTagCompound());
            jp.setInteger("partyID", entry.getID());
            json.appendTag(jp);
        }

        return json;
    }

    @Override
    public synchronized void readFromNBT(NBTTagList json, boolean merge) {
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
}
