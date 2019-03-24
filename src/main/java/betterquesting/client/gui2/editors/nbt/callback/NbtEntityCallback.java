package betterquesting.client.gui2.editors.nbt.callback;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.JsonHelper;

public class NbtEntityCallback implements ICallback<Entity>
{
	private final NBTTagCompound json;
	
	public NbtEntityCallback(NBTTagCompound json)
	{
		this.json = json;
	}
	
	public void setValue(Entity entity)
	{
		Entity baseEntity;
		
		if(entity != null)
		{
			baseEntity = entity;
		} else
		{
			baseEntity = new EntityPig(Minecraft.getMinecraft().theWorld);
		}
		
		JsonHelper.ClearCompoundTag(json);
		JsonHelper.EntityToJson(baseEntity, json);
	}
}
