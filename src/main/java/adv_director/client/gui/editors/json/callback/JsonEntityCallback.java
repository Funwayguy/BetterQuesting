package adv_director.client.gui.editors.json.callback;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import adv_director.api.misc.ICallback;
import adv_director.api.utils.JsonHelper;
import com.google.gson.JsonObject;

public class JsonEntityCallback implements ICallback<Entity>
{
	private Entity baseEntity = null;
	private final JsonObject json;
	
	public JsonEntityCallback(JsonObject json)
	{
		this(json, new EntityPig(Minecraft.getMinecraft().theWorld));
	}
	
	public JsonEntityCallback(JsonObject json, Entity stack)
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
			this.baseEntity = new EntityPig(Minecraft.getMinecraft().theWorld);
		}
		
		json.entrySet().clear();
		JsonHelper.EntityToJson(baseEntity, json);
	}
	
	public JsonObject getJsonObject()
	{
		return json;
	}
	
	public Entity getEntity()
	{
		return baseEntity;
	}
}
