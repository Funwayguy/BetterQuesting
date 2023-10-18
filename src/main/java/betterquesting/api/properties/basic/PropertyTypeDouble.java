package betterquesting.api.properties.basic;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.util.ResourceLocation;

public class PropertyTypeDouble extends PropertyTypeBase<Double> {
  public PropertyTypeDouble(ResourceLocation key, Double def) {
    super(key, def);
  }

  @Override
  public Double readValue(NBTBase nbt) {
    if (!(nbt instanceof NBTPrimitive)) {
      return getDefault();
    }

    return ((NBTPrimitive) nbt).getDouble();
  }

  @Override
  public NBTBase writeValue(Double value) {
    if (value == null) {
      return new NBTTagDouble(getDefault());
    }

    return new NBTTagDouble(value);
  }
}
