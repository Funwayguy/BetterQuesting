package betterquesting.storage;

import betterquesting.api.storage.INameCache;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

public final class NameCache implements INameCache {
    public static final NameCache INSTANCE = new NameCache();

    // TODO: Label known names as offline/online and convert accordingly?
    private final HashMap<UUID, NBTTagCompound> cache = new HashMap<>();

    @Override
    public synchronized boolean updateName(@Nonnull EntityPlayerMP player) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        NBTTagCompound tag = cache.computeIfAbsent(player.getGameProfile().getId(), (key) -> new NBTTagCompound());

        String name = player.getGameProfile().getName();
        boolean isOP = server.getPlayerList().canSendCommands(player.getGameProfile());

        if (!tag.getString("name").equals(name) || tag.getBoolean("isOP") != isOP) {
            tag.setString("name", name);
            tag.setBoolean("isOP", isOP);
            return true;
        }

        return false;
    }

    @Override
    public synchronized String getName(@Nonnull UUID uuid) {
        NBTTagCompound tag = cache.get(uuid);
        return tag == null ? uuid.toString() : tag.getString("name");
    }

    @Override
    public synchronized UUID getUUID(@Nonnull String name) {
        for (Entry<UUID, NBTTagCompound> entry : cache.entrySet()) {
            if (entry.getValue().getString("name").equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }

        return null;
    }

    @Override
    public synchronized boolean isOP(@Nonnull UUID uuid) {
        NBTTagCompound tag = cache.get(uuid);
        return tag != null && tag.getBoolean("isOP");
    }

    @Override
    public synchronized int size() {
        return cache.size();
    }

    @Override
    public synchronized NBTTagList writeToNBT(NBTTagList nbt, @Nullable List<UUID> users) {
        for (Entry<UUID, NBTTagCompound> entry : cache.entrySet()) {
            if (users != null && !users.contains(entry.getKey())) continue;
            NBTTagCompound jn = new NBTTagCompound();
            jn.setString("uuid", entry.getKey().toString());
            jn.setString("name", entry.getValue().getString("name"));
            jn.setBoolean("isOP", entry.getValue().getBoolean("isOP"));
            nbt.appendTag(jn);
        }

        return nbt;
    }

    @Override
    public synchronized void readFromNBT(NBTTagList nbt, boolean merge) {
        if (!merge) cache.clear();
        for (int i = 0; i < nbt.tagCount(); i++) {
            NBTTagCompound jn = nbt.getCompoundTagAt(i);

            try {
                UUID uuid = UUID.fromString(jn.getString("uuid"));
                String name = jn.getString("name");
                boolean isOP = jn.getBoolean("isOP");

                NBTTagCompound j2 = new NBTTagCompound();
                j2.setString("name", name);
                j2.setBoolean("isOP", isOP);
                cache.put(uuid, j2);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public synchronized void reset() {
        cache.clear();
        nameCache = null;
    }

    private List<String> nameCache = null;

    @Override
    public synchronized List<String> getAllNames() {
        if (nameCache != null) return nameCache;

        nameCache = new ArrayList<>();

        for (NBTTagCompound tag : cache.values()) {
            if (tag != null && tag.hasKey("name", 8)) {
                nameCache.add(tag.getString("name"));
            }
        }

        return Collections.unmodifiableList(nameCache);
    }
}
