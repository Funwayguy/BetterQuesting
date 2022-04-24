package betterquesting.client.gui2.editors.nbt.callback;

import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

public class NbtItemCallback implements ICallback<BigItemStack> {
    private final NBTTagCompound json;

    public NbtItemCallback(NBTTagCompound json) {
        this.json = json;
    }

    public void setValue(BigItemStack stack) {
        BigItemStack baseStack;

        if (stack != null) {
            baseStack = stack;
        } else {
            baseStack = new BigItemStack(Blocks.STONE);
        }

        JsonHelper.ClearCompoundTag(json);
        JsonHelper.ItemStackToJson(baseStack, json);
    }
}
