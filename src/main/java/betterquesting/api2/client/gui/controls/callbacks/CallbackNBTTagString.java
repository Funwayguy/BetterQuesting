package betterquesting.api2.client.gui.controls.callbacks;

import betterquesting.api.misc.ICallback;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public class CallbackNBTTagString implements ICallback<String> {
    private final NBTBase tag;
    private final String sKey;
    private final int iKey;

    public CallbackNBTTagString(NBTTagCompound tag, String key) {
        this.tag = tag;
        this.sKey = key;
        this.iKey = -1;
    }

    public CallbackNBTTagString(NBTTagList tag, int key) {
        this.tag = tag;
        this.sKey = null;
        this.iKey = key;
    }

    @Override
    public void setValue(String value) {
        if (tag.getId() == 10) {
            ((NBTTagCompound) tag).setString(sKey, value);
        } else {
            ((NBTTagList) tag).set(iKey, new NBTTagString(value));
        }
    }
}
