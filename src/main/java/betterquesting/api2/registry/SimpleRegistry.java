package betterquesting.api2.registry;

import betterquesting.core.BetterQuesting;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Callable;

public class SimpleRegistry<T> {
    private final HashMap<ResourceLocation, Callable<T>> factories = new HashMap<>();

    public void register(@Nonnull ResourceLocation idname, @Nonnull Callable<T> factory) {
        if (factories.containsKey(idname)) {
            throw new IllegalArgumentException("Cannot register duplicate factory or registry name");
        }

        factories.put(idname, factory);
    }

    @Nullable
    public T createNew(@Nonnull ResourceLocation idName) {
        Callable<T> fact = factories.get(idName);
        try {
            return fact == null ? null : fact.call();
        } catch (Exception e) {
            BetterQuesting.logger.error("Registry failed to instantiate new object with ID: " + idName.toString(), e);
            return null;
        }
    }

    public Set<ResourceLocation> getAll() {
        return Collections.unmodifiableSet(factories.keySet());
    }
}
