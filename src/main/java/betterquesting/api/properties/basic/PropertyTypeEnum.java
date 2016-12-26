package betterquesting.api.properties.basic;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class PropertyTypeEnum<E extends Enum<E>> extends PropertyTypeBase<E>
{
	private final Class<E> eClazz;
	
	public PropertyTypeEnum(ResourceLocation key, E def)
	{
		super(key, def);
		
		eClazz = def.getDeclaringClass();
	}
	
	@Override
	public E readValue(JsonElement json)
	{
		if(json == null || !json.isJsonPrimitive())
		{
			return this.getDefault();
		}
		
		try
		{
			return Enum.valueOf(eClazz, json.getAsString());
		} catch(Exception e)
		{
			return this.getDefault();
		}
	}
	
	@Override
	public JsonElement writeValue(E value)
	{
		if(value == null)
		{
			return new JsonPrimitive(this.getDefault().toString());
		}
		
		return new JsonPrimitive(value.toString());
	}
}
