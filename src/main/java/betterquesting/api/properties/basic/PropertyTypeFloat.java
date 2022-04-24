package betterquesting.api.properties.basic;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeFloat extends PropertyTypeBase<Float> {
    public PropertyTypeFloat(ResourceLocation key, Float def) {
        super(key, def);
    }

    @Override
    public Float readValue(NBTBase nbt) {
        if (nbt == null || !(nbt instanceof NBTPrimitive)) {
            return this.getDefault();
        }

        return ((NBTPrimitive) nbt).getFloat();
    }

    @Override
    public NBTBase writeValue(Float value) {
        if (value == null) {
            return new NBTTagFloat(this.getDefault());
        }

        return new NBTTagFloat(value);
    }
}
