package betterquesting.api2.client.gui.controls.callbacks;

import betterquesting.api.misc.ICallback;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

import javax.annotation.Nonnull;

public class CallbackNBTTagString implements ICallback<String>
{
    private final INBT tag;
    private final String sKey;
    private final int iKey;
    
    public CallbackNBTTagString(@Nonnull CompoundNBT tag, @Nonnull String key)
    {
        this.tag = tag;
        this.sKey = key;
        this.iKey = -1;
    }
    
    public CallbackNBTTagString(@Nonnull ListNBT tag, int key)
    {
        this.tag = tag;
        this.sKey = null;
        this.iKey = key;
    }
    
    @Override
    public void setValue(String value)
    {
        if(tag.getId() == 10 && sKey != null)
        {
            ((CompoundNBT)tag).putString(sKey, value);
        } else if(tag.getId() == 9)
        {
            ((ListNBT)tag).set(iKey, StringNBT.valueOf(value));
        }
    }
}
