package betterquesting.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.core.BetterQuesting;
import betterquesting.storage.PropertyContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;

public class PartyInstance implements IParty {
    private final HashMap<UUID, EnumPartyStatus> members = new HashMap<>();
    private List<UUID> memCache = null;

    private final PropertyContainer pInfo = new PropertyContainer();

    public PartyInstance() {
        this.setupProps();
    }

    private void setupProps() {
        setupValue(NativeProps.NAME, "New Party");
    }

    private <T> void setupValue(IPropertyType<T> prop) {
        this.setupValue(prop, prop.getDefault());
    }

    private <T> void setupValue(IPropertyType<T> prop, T def) {
        pInfo.setProperty(prop, pInfo.getProperty(prop, def));
    }

    private void refreshCache() {
        memCache = Collections.unmodifiableList(new ArrayList<>(members.keySet()));
    }

    @Override
    public IPropertyContainer getProperties() {
        return pInfo;
    }

    @Override
    public void kickUser(@Nonnull UUID uuid) {
        if (!members.containsKey(uuid)) return;

        EnumPartyStatus old = members.get(uuid);
        members.remove(uuid);

        if (old == EnumPartyStatus.OWNER && members.size() > 0) hostMigrate();
        refreshCache();
    }

    @Override
    public void setStatus(@Nonnull UUID uuid, @Nonnull EnumPartyStatus priv) {
        EnumPartyStatus old = members.get(uuid);
        if (old == priv) return;

        members.put(uuid, priv);

        if (priv == EnumPartyStatus.OWNER) // Check and drop previous owner(s)
        {
            for (UUID mem : getMembers()) {
                if (mem != uuid && members.get(mem) == EnumPartyStatus.OWNER) {
                    members.put(mem, EnumPartyStatus.ADMIN);
                }
            }
        } else if (old == EnumPartyStatus.OWNER) {
            UUID migrate = null;

            // Find new owner
            for (UUID mem : getMembers()) {
                if (mem == uuid) continue;

                if (members.get(mem) == EnumPartyStatus.ADMIN) {
                    migrate = mem;
                    break;
                } else if (migrate == null) {
                    migrate = mem;
                }
            }

            // No other valid owners found
            if (migrate == null) {
                members.put(uuid, old);
                return;
            } else {
                members.put(migrate, EnumPartyStatus.OWNER);
            }
        }

        refreshCache();
    }

    @Override
    public EnumPartyStatus getStatus(@Nonnull UUID uuid) {
        return members.get(uuid);
    }

    @Override
    public List<UUID> getMembers() {
        if (memCache == null) refreshCache();
        return memCache;
    }

    private void hostMigrate() {
        // Pre check for existing owners
        for (Entry<UUID, EnumPartyStatus> entry : members.entrySet()) {
            if (entry.getValue() == EnumPartyStatus.OWNER) {
                return;
            }
        }

        UUID migrate = null;

        for (Entry<UUID, EnumPartyStatus> entry : members.entrySet()) {
            EnumPartyStatus status = entry.getValue();

            if (status == EnumPartyStatus.ADMIN || status == EnumPartyStatus.OWNER) {
                migrate = entry.getKey();
                break;
            } else if (migrate == null) {
                migrate = entry.getKey();
            }
        }

        if (migrate != null) {
            members.put(migrate, EnumPartyStatus.OWNER);
        } else {
            BetterQuesting.logger.error("Failed to find suitable host to migrate party " + this.pInfo.getProperty(NativeProps.NAME));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound json) {
        NBTTagList memJson = new NBTTagList();
        for (Entry<UUID, EnumPartyStatus> mem : members.entrySet()) {
            NBTTagCompound jm = new NBTTagCompound();
            jm.setString("uuid", mem.getKey().toString());
            jm.setString("status", mem.getValue().toString());
            memJson.appendTag(jm);
        }
        json.setTag("members", memJson);

        json.setTag("properties", pInfo.writeToNBT(new NBTTagCompound()));

        return json;
    }

    @Override
    public void readFromNBT(NBTTagCompound jObj) {
        if (jObj.hasKey("properties", 10)) {
            pInfo.readFromNBT(jObj.getCompoundTag("properties"));
        } else // Legacy stuff
        {
            pInfo.readFromNBT(new NBTTagCompound());
            pInfo.setProperty(NativeProps.NAME, jObj.getString("name"));
        }

        members.clear();
        NBTTagList memList = jObj.getTagList("members", 10);
        for (int i = 0; i < memList.tagCount(); i++) {
            try {
                NBTTagCompound jMem = memList.getCompoundTagAt(i);
                if (!jMem.hasKey("uuid", 8) || !jMem.hasKey("status")) continue;
                UUID uuid = UUID.fromString(jMem.getString("uuid"));
                EnumPartyStatus priv = EnumPartyStatus.valueOf(jMem.getString("status"));
                members.put(uuid, priv);
            } catch (Exception ignored) {
            }
        }

        refreshCache();
        this.setupProps();
    }

    @Override
    public NBTTagCompound writeProperties(NBTTagCompound nbt) {
        return pInfo.writeToNBT(nbt);
    }

    @Override
    public void readProperties(NBTTagCompound nbt) {
        pInfo.readFromNBT(nbt);
    }
}
