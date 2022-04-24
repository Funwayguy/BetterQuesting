package betterquesting.api2.registry;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

@Deprecated
public interface IRegistry<T extends IFactory<E>, E> {
    void register(T factory);

    T getFactory(ResourceLocation idName);

    @Nullable
    E createNew(ResourceLocation idName);

    List<T> getAll();
}
