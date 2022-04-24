package betterquesting.api.properties;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.ResourceLocation;

public interface IPropertyType<T> {
    ResourceLocation getKey();

    T getDefault();

    T readValue(NBTBase nbt);

    NBTBase writeValue(T value);
}
