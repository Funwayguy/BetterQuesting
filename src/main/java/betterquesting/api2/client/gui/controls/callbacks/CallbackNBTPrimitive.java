package betterquesting.api2.client.gui.controls.callbacks;

import betterquesting.api.misc.ICallback;
import net.minecraft.nbt.*;

public class CallbackNBTPrimitive<T extends Number> implements ICallback<T>
{
    private final INBT tag;
    private final String sKey;
    private final int iKey;
    private final int tagID;
    
    public CallbackNBTPrimitive(CompoundNBT tag, String key, Class<T> c)
    {
        this.tag = tag;
        this.sKey = key;
        this.iKey = -1;
        this.tagID = getTagID(c);
    }
    
    public CallbackNBTPrimitive(ListNBT tag, int key, Class<T> c)
    {
        this.tag = tag;
        this.sKey = null;
        this.iKey = key;
        this.tagID = getTagID(c);
    }
    
    @Override
    public void setValue(T value)
    {
        if(tag.getId() == 10)
        {
            setCompoundTag((CompoundNBT)tag, value);
        } else
        {
            setListTag((ListNBT)tag, value);
        }
    }
    
    private void setCompoundTag(CompoundNBT compound, T value)
    {
        switch(tagID)
        {
            case 1:
                compound.putByte(sKey, value.byteValue());
                break;
            case 2:
                compound.putShort(sKey, value.shortValue());
                break;
            case 3:
                compound.putInt(sKey, value.intValue());
                break;
            case 4:
                compound.putLong(sKey, value.longValue());
                break;
            case 5:
                compound.putFloat(sKey, value.floatValue());
                break;
            case 6:
                compound.putDouble(sKey, value.doubleValue());
                break;
        }
    }
    
    private void setListTag(ListNBT list, T value)
    {
        switch(tagID)
        {
            case 1:
                list.set(iKey, ByteNBT.valueOf(value.byteValue()));
                break;
            case 2:
                list.set(iKey, ShortNBT.valueOf(value.shortValue()));
                break;
            case 3:
                list.set(iKey, IntNBT.valueOf(value.intValue()));
                break;
            case 4:
                list.set(iKey, LongNBT.valueOf(value.longValue()));
                break;
            case 5:
                list.set(iKey, FloatNBT.valueOf(value.floatValue()));
                break;
            case 6:
                list.set(iKey, DoubleNBT.valueOf(value.doubleValue()));
                break;
        }
    }
    
    private int getTagID(Class<T> c)
    {
        // There's gotta be a better way of doing this right?
        if(c == Byte.class)
        {
            return 1;
        } else if(c == Short.class)
        {
            return 2;
        } else if(c == Integer.class)
        {
            return 3;
        } else if(c == Long.class)
        {
            return 4;
        } else if(c == Float.class)
        {
            return 5;
        } else if(c == Double.class)
        {
            return 6;
        }
        
        return 0; // WTF?
    }
}
