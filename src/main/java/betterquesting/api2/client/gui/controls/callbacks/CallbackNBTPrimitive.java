package betterquesting.api2.client.gui.controls.callbacks;

import betterquesting.api.misc.ICallback;
import net.minecraft.nbt.*;

public class CallbackNBTPrimitive<T extends Number> implements ICallback<T> {
    private final NBTBase tag;
    private final String sKey;
    private final int iKey;
    private final int tagID;

    public CallbackNBTPrimitive(NBTTagCompound tag, String key, Class<T> c) {
        this.tag = tag;
        this.sKey = key;
        this.iKey = -1;
        this.tagID = getTagID(c);
    }

    public CallbackNBTPrimitive(NBTTagList tag, int key, Class<T> c) {
        this.tag = tag;
        this.sKey = null;
        this.iKey = key;
        this.tagID = getTagID(c);
    }

    @Override
    public void setValue(T value) {
        if (tag.getId() == 10) {
            setCompoundTag((NBTTagCompound) tag, value);
        } else {
            setListTag((NBTTagList) tag, value);
        }
    }

    private void setCompoundTag(NBTTagCompound compound, T value) {
        switch (tagID) {
            case 1:
                compound.setByte(sKey, value.byteValue());
                break;
            case 2:
                compound.setShort(sKey, value.shortValue());
                break;
            case 3:
                compound.setInteger(sKey, value.intValue());
                break;
            case 4:
                compound.setLong(sKey, value.longValue());
                break;
            case 5:
                compound.setFloat(sKey, value.floatValue());
                break;
            case 6:
                compound.setDouble(sKey, value.doubleValue());
                break;
        }
    }

    private void setListTag(NBTTagList list, T value) {
        switch (tagID) {
            case 1:
                list.set(iKey, new NBTTagByte(value.byteValue()));
                break;
            case 2:
                list.set(iKey, new NBTTagShort(value.shortValue()));
                break;
            case 3:
                list.set(iKey, new NBTTagInt(value.intValue()));
                break;
            case 4:
                list.set(iKey, new NBTTagLong(value.longValue()));
                break;
            case 5:
                list.set(iKey, new NBTTagFloat(value.floatValue()));
                break;
            case 6:
                list.set(iKey, new NBTTagDouble(value.doubleValue()));
                break;
        }
    }

    private int getTagID(Class<T> c) {
        // There's gotta be a better way of doing this right?
        if (c == Byte.class) {
            return 1;
        } else if (c == Short.class) {
            return 2;
        } else if (c == Integer.class) {
            return 3;
        } else if (c == Long.class) {
            return 4;
        } else if (c == Float.class) {
            return 5;
        } else if (c == Double.class) {
            return 6;
        }

        return 0; // WTF?
    }
}
