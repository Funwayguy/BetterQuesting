package betterquesting.api.properties.basic;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeLong extends PropertyTypeBase<Long> {
    public PropertyTypeLong(ResourceLocation key, Long def) {
        super(key, def);
    }

    @Override
    public Long readValue(NBTBase nbt) {
        if (nbt == null || !(nbt instanceof NBTPrimitive)) {
            return this.getDefault();
        }

        return ((NBTPrimitive) nbt).getLong();
    }

    @Override
    public NBTBase writeValue(Long value) {
        if (value == null) {
            return new NBTTagLong(this.getDefault());
        }

        return new NBTTagLong(value);
    }
}
