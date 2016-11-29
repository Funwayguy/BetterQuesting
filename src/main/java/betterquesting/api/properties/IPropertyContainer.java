package betterquesting.api.properties;

import betterquesting.api.misc.IJsonSaveLoad;
import com.google.gson.JsonObject;

public interface IPropertyContainer extends IJsonSaveLoad<JsonObject>
{
	public <T> T getProperty(IPropertyType<T> prop);
	public <T> T getProperty(IPropertyType<T> prop, T def);
	
	public boolean hasProperty(IPropertyType<?> prop);
	
	public <T> void setProperty(IPropertyType<T> prop, T value);
}