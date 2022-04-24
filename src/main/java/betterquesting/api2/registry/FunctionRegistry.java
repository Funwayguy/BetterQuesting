package betterquesting.api2.registry;

import betterquesting.core.BetterQuesting;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

public class FunctionRegistry<T, E> {
    private final HashMap<ResourceLocation, Function<E, T>> factories = new HashMap<>();
    private final HashMap<ResourceLocation, E> def_args = new HashMap<>();

    public void register(@Nonnull ResourceLocation idname, @Nonnull Function<E, T> factory, E template) {
        if (factories.containsKey(idname)) {
            throw new IllegalArgumentException("Cannot register duplicate factory or registry name");
        }

        factories.put(idname, factory);
        def_args.put(idname, template);
    }

    @Nullable
    public T createNew(@Nonnull ResourceLocation idName) {
        E arg = def_args.get(idName);
        if (arg != null) return createNew(idName, arg);

        BetterQuesting.logger.error("Registry failed to instantiate new object with ID: " + idName.toString());
        return null;
    }

    @Nullable
    public T createNew(@Nonnull ResourceLocation idName, @Nonnull E info) {
        Function<E, T> fact = factories.get(idName);
        try {
            return fact == null ? null : fact.apply(info);
        } catch (Exception e) {
            BetterQuesting.logger.error("Registry failed to instantiate new object with ID: " + idName.toString(), e);
            return null;
        }
    }

    @Nullable
    public E getTemplate(@Nonnull ResourceLocation idname) {
        return def_args.get(idname);
    }

    @Nonnull
    public Set<ResourceLocation> getAll() {
        return Collections.unmodifiableSet(factories.keySet());
    }
}
