package betterquesting.storage;

import java.util.Map.Entry;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PropertyContainer implements IPropertyContainer
{
	private JsonObject jInfo = new JsonObject();
	
	@Override
	public <T> T getProperty(IPropertyType<T> prop)
	{
		if(prop == null)
		{
			return null;
		}
		
		return getProperty(prop, prop.getDefault());
	}
	
	@Override
	public <T> T getProperty(IPropertyType<T> prop, T def)
	{
		if(prop == null)
		{
			return null;
		}
		
		JsonElement jProp = getJsonDomain(prop.getKey()).get(prop.getKey().getResourcePath());
		
		if(jProp == null)
		{
			return def;
		}
		
		return prop.readValue(jProp);
	}
	
	@Override
	public boolean hasProperty(IPropertyType<?> prop)
	{
		if(prop == null)
		{
			return false;
		}
		
		return getJsonDomain(prop.getKey()).has(prop.getKey().getResourcePath());
	}
	
	@Override
	public <T> void setProperty(IPropertyType<T> prop, T value)
	{
		if(prop == null || value == null)
		{
			return;
		}
		
		JsonObject dom = getJsonDomain(prop.getKey());
		dom.add(prop.getKey().getResourcePath(), prop.writeValue(value));
		jInfo.add(prop.getKey().getResourceDomain(), dom);
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		for(Entry<String,JsonElement> entry : jInfo.entrySet())
		{
			json.add(entry.getKey(), entry.getValue());
		}
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		jInfo = new JsonObject();
		
		for(Entry<String,JsonElement> entry : json.entrySet())
		{
			jInfo.add(entry.getKey(), entry.getValue());
		}
	}
	
	private JsonObject getJsonDomain(ResourceLocation res)
	{
		return JsonHelper.GetObject(jInfo, res.getResourceDomain());
	}
}
