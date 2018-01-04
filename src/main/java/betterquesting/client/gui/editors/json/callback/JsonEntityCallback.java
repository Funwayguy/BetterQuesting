package betterquesting.client.gui.editors.json.callback;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.JsonHelper;

public class JsonEntityCallback implements ICallback<Entity>
{
	private Entity baseEntity = null;
	private final NBTTagCompound json;
	
	public JsonEntityCallback(NBTTagCompound json)
	{
		this(json, new EntityPig(Minecraft.getMinecraft().world));
	}
	
	public JsonEntityCallback(NBTTagCompound json, Entity stack)
	{
		this.json = json;
		this.baseEntity = stack;
	}
	
	public void setValue(Entity entity)
	{
		if(entity != null)
		{
			this.baseEntity = entity;
		} else
		{
			this.baseEntity = new EntityPig(Minecraft.getMinecraft().world);
		}
		
		JsonHelper.ClearCompoundTag(json);
		JsonHelper.EntityToJson(baseEntity, json);
	}
	
	public NBTTagCompound getJsonObject()
	{
		return json;
	}
	
	public Entity getEntity()
	{
		return baseEntity;
	}
}
