package betterquesting.api.properties.basic;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeBoolean extends PropertyTypeBase<Boolean> {
    public PropertyTypeBoolean(ResourceLocation key, Boolean def) {
        super(key, def);
    }

    @Override
    public Boolean readValue(NBTBase nbt) {
        if (nbt == null || nbt.getId() < 1 || nbt.getId() > 6) {
            return this.getDefault();
        }

        try {
            return ((NBTPrimitive) nbt).getByte() > 0;
        } catch (Exception e) {
            return this.getDefault();
        }
    }

    @Override
    public NBTBase writeValue(Boolean value) {
        if (value == null) {
            return new NBTTagByte(this.getDefault() ? (byte) 1 : (byte) 0);
        }

        return new NBTTagByte(value ? (byte) 1 : (byte) 0);
    }
}