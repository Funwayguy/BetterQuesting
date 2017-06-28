package adv_director.api.misc;

import adv_director.api.enums.EnumSaveType;
import com.google.gson.JsonElement;

public interface IJsonSaveLoad<T extends JsonElement>
{
	public T writeToJson(T json, EnumSaveType saveType);
	public void readFromJson(T json, EnumSaveType saveType);
}
