package betterquesting.api2.registry;

import betterquesting.api.misc.IFactory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class SimpleRegistry<T> implements IRegistry<T>
{
    private final HashMap<ResourceLocation, IFactory<T>> factories = new HashMap<>();
    private final IFactory<T> placeholder;
    
    public SimpleRegistry(IFactory<T> placeholder)
    {
        this.placeholder = placeholder;
    }
    
    @Override
    public void register(IFactory<T> factory)
    {
        if(factory == null || factory.getRegistryName() == null)
        {
            throw new NullPointerException("Factory or registry name is null!");
        } else if(factories.containsKey(factory.getRegistryName()) || factories.containsValue(factory))
        {
            throw new IllegalArgumentException("Cannot register duplicate factory or registry name");
        }
        
        factories.put(factory.getRegistryName(), factory);
    }
    
    @Override
    public IFactory<T> getFactory(ResourceLocation idName)
    {
        return factories.get(idName);
    }
    
    @Override
    public IFactory<T> getPlaceholder()
    {
        return placeholder;
    }
    
    @Override
    public T createNew(ResourceLocation idName)
    {
        IFactory<T> fact = getFactory(idName);
        
        return fact == null ? null : fact.createNew();
    }
    
    @Override
    public T loadFromNBT(ResourceLocation idName, NBTTagCompound nbt)
    {
        IFactory<T> fact = getFactory(idName);
        fact = fact != null ? fact : getPlaceholder();
        return fact.loadFromNBT(nbt);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public IFactory<T>[] getAll()
    {
        return factories.values().toArray(new IFactory[0]);
    }
}
