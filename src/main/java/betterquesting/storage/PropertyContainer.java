package betterquesting.storage;

import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class PropertyContainer implements IPropertyContainer, INBTSaveLoad<CompoundNBT>
{
	private final CompoundNBT nbtInfo = new CompoundNBT();
	
	@Override
	public synchronized <T> T getProperty(IPropertyType<T> prop)
	{
		if(prop == null) return null;
		
		return getProperty(prop, prop.getDefault());
	}
	
	@Override
	public synchronized <T> T getProperty(IPropertyType<T> prop, T def)
	{
		if(prop == null) return def;
		
        CompoundNBT jProp = getDomain(prop.getKey());

        if(!jProp.contains(prop.getKey().getPath())) return def;

        return prop.readValue(jProp.get(prop.getKey().getPath()));
	}
	
	@Override
	public synchronized boolean hasProperty(IPropertyType<?> prop)
	{
		if(prop == null) return false;
        return getDomain(prop.getKey()).contains(prop.getKey().getPath());
	}
    
    @Override
    public synchronized void removeProperty(IPropertyType<?> prop)
    {
        if(prop == null) return;
        CompoundNBT jProp = getDomain(prop.getKey());
        
        if(!jProp.contains(prop.getKey().getPath())) return;
        
        jProp.remove(prop.getKey().getPath());
        
        if(jProp.isEmpty()) nbtInfo.remove(prop.getKey().getNamespace());
    }
    
    @Override
	public synchronized <T> void setProperty(IPropertyType<T> prop, T value)
	{
		if(prop == null || value == null) return;
        CompoundNBT dom = getDomain(prop.getKey());
        dom.put(prop.getKey().getPath(), prop.writeValue(value));
        nbtInfo.put(prop.getKey().getNamespace(), dom);
	}
    
    @Override
    public synchronized void removeAllProps()
    {
        List<String> keys = new ArrayList<>(nbtInfo.keySet());
        for(String key : keys) nbtInfo.remove(key);
    }
    
    @Override
	public synchronized CompoundNBT writeToNBT(CompoundNBT nbt)
	{
        nbt.merge(nbtInfo);
        return nbt;
	}
	
	@Override
	public synchronized void readFromNBT(CompoundNBT nbt)
	{
        for(String key : nbtInfo.keySet()) nbtInfo.remove(key);
        nbtInfo.merge(nbt);
        
        // TODO: FIX CASING
        /*List<String> keys = new ArrayList<>(nbtInfo.getKeySet());
        for(nbt)
        {
        
        }*/
	}
	
	private CompoundNBT getDomain(ResourceLocation res)
	{
		return nbtInfo.getCompound(res.getNamespace());
	}
}
