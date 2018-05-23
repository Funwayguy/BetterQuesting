package betterquesting.api2.registry;

import betterquesting.api.misc.IFactory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public interface IRegistry<T>
{
    void register(IFactory<T> factory);
    IFactory<T> getFactory(ResourceLocation idName);
    IFactory<T> getPlaceholder();
    
    T createNew(ResourceLocation idName);
    T loadFromNBT(ResourceLocation idName, NBTTagCompound nbt);
    
    IFactory<T>[] getAll();
}
