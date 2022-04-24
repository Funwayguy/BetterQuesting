package betterquesting.api2.registry;

import net.minecraft.util.ResourceLocation;

@Deprecated // Stop... just use lambdas
public interface IFactory<T> {
    ResourceLocation getRegistryName();

    T createNew();
}
