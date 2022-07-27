package betterquesting.api2.registry;

import net.minecraft.util.ResourceLocation;

public interface IFactory<T> {
    ResourceLocation getRegistryName();

    T createNew();
}
