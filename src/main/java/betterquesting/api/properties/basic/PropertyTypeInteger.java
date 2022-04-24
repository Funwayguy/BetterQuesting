package betterquesting.api.properties.basic;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeInteger extends PropertyTypeBase<Integer> {
    public PropertyTypeInteger(ResourceLocation key, Integer def) {
        super(key, def);
    }

    @Override
    public Integer readValue(NBTBase nbt) {
        if (nbt == null || !(nbt instanceof NBTPrimitive)) {
            return this.getDefault();
        }

        return ((NBTPrimitive) nbt).getInt();
    }

    @Override
    public NBTBase writeValue(Integer value) {
        if (value == null) {
            return new NBTTagInt(this.getDefault());
        }

        return new NBTTagInt(value);
    }
}
