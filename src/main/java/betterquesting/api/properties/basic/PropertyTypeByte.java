package betterquesting.api.properties.basic;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeByte extends PropertyTypeBase<Byte> {
  public PropertyTypeByte(ResourceLocation key, Byte def) {
    super(key, def);
  }

  @Override
  public Byte readValue(NBTBase nbt) {
    if (!(nbt instanceof NBTPrimitive)) {
      return getDefault();
    }

    return ((NBTPrimitive) nbt).getByte();
  }

  @Override
  public NBTBase writeValue(Byte value) {
    if (value == null) {
      return new NBTTagByte(getDefault());
    }

    return new NBTTagByte(value);
  }
}
