package betterquesting.client.gui2.editors.nbt.callback;

import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.JsonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.nbt.CompoundNBT;

public class NbtEntityCallback implements ICallback<Entity>
{
	private final CompoundNBT json;
	
	public NbtEntityCallback(CompoundNBT json)
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
			baseEntity = new PigEntity(EntityType.PIG, Minecraft.getInstance().world);
		}
		
		JsonHelper.ClearCompoundTag(json);
		JsonHelper.EntityToJson(baseEntity, json);
	}
}
