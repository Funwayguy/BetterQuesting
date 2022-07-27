package betterquesting.storage;

import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api2.storage.INBTSaveLoad;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PropertyContainer implements IPropertyContainer, INBTSaveLoad<NBTTagCompound> {
    private final NBTTagCompound nbtInfo = new NBTTagCompound();

    @Override
    public <T> T getProperty(IPropertyType<T> prop) {
        if (prop == null) return null;

        return getProperty(prop, prop.getDefault());
    }

    @Override
    public <T> T getProperty(IPropertyType<T> prop, T def) {
        if (prop == null) return def;

        synchronized (nbtInfo) {
            NBTTagCompound jProp = getDomain(prop.getKey());

            if (!jProp.hasKey(prop.getKey().getResourcePath())) return def;

            return prop.readValue(jProp.getTag(prop.getKey().getResourcePath()));
        }
    }

    @Override
    public boolean hasProperty(IPropertyType<?> prop) {
        if (prop == null) return false;

        synchronized (nbtInfo) {
            return getDomain(prop.getKey()).hasKey(prop.getKey().getResourcePath());
        }
    }

    @Override
    public void removeProperty(IPropertyType<?> prop) {
        if (prop == null) return;

        synchronized (nbtInfo) {
            NBTTagCompound jProp = getDomain(prop.getKey());

            if (!jProp.hasKey(prop.getKey().getResourcePath())) return;

            jProp.removeTag(prop.getKey().getResourcePath());

            if (jProp.hasNoTags()) nbtInfo.removeTag(prop.getKey().getResourceDomain());
        }
    }

    @Override
    public <T> void setProperty(IPropertyType<T> prop, T value) {
        if (prop == null || value == null) return;

        synchronized (nbtInfo) {
            NBTTagCompound dom = getDomain(prop.getKey());
            dom.setTag(prop.getKey().getResourcePath(), prop.writeValue(value));
            nbtInfo.setTag(prop.getKey().getResourceDomain(), dom);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeAllProps() {
        synchronized (nbtInfo) {
            List<String> keys = new ArrayList<>((Set<String>) nbtInfo.func_150296_c());
            for (String key : keys) nbtInfo.removeTag(key);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        synchronized (nbtInfo) {
            merge(nbt, nbtInfo);
            return nbt;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readFromNBT(NBTTagCompound nbt) {
        synchronized (nbtInfo) {
            for (String key : (Set<String>) nbtInfo.func_150296_c()) nbtInfo.removeTag(key);
            merge(nbtInfo, nbt);
        }
    }

    private NBTTagCompound getDomain(ResourceLocation res) {
        return nbtInfo.getCompoundTag(res.getResourceDomain());
    }

    @SuppressWarnings("unchecked")
    private void merge(NBTTagCompound parent, NBTTagCompound other) {
        for (String s : (Set<String>) other.func_150296_c()) {
            NBTBase nbtbase = other.getTag(s);

            if (nbtbase.getId() == 10) {
                if (parent.hasKey(s, 10)) {
                    NBTTagCompound nbttagcompound = parent.getCompoundTag(s);
                    merge(nbttagcompound, (NBTTagCompound) nbtbase);
                } else {
                    parent.setTag(s, nbtbase.copy());
                }
            } else {
                parent.setTag(s, nbtbase.copy());
            }
        }
    }
}
