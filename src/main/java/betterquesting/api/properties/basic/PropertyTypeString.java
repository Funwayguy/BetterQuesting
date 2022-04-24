package betterquesting.api.properties.basic;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeString extends PropertyTypeBase<String> {
    public PropertyTypeString(ResourceLocation key, String def) {
        super(key, def);
    }

    @Override
    public String readValue(NBTBase nbt) {
        if (nbt == null || nbt.getId() != 8) {
            return this.getDefault();
        }

        return ((NBTTagString) nbt).getString();
    }

    @Override
    public NBTBase writeValue(String value) {
        if (value == null) {
            return new NBTTagString(this.getDefault());
        }

        return new NBTTagString(value);
    }
}
