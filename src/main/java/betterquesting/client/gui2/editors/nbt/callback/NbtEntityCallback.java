package betterquesting.client.gui2.editors.nbt.callback;

import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.JsonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.nbt.NBTTagCompound;

public class NbtEntityCallback implements ICallback<Entity> {
    private final NBTTagCompound json;

    public NbtEntityCallback(NBTTagCompound json) {
        this.json = json;
    }

    public void setValue(Entity entity) {
        Entity baseEntity;

        if (entity != null) {
            baseEntity = entity;
        } else {
            baseEntity = new EntityPig(Minecraft.getMinecraft().world);
        }

        JsonHelper.ClearCompoundTag(json);
        JsonHelper.EntityToJson(baseEntity, json);
    }
}
