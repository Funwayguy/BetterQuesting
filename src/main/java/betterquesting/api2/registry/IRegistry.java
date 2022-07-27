package betterquesting.api2.registry;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

public interface IRegistry<T extends IFactory<E>, E> {
    void register(T factory);

    T getFactory(ResourceLocation idName);

    @Nullable
    E createNew(ResourceLocation idName);

    List<T> getAll();
}
