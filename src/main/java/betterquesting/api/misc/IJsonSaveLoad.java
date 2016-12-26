package betterquesting.api.misc;

import betterquesting.api.enums.EnumSaveType;
import com.google.gson.JsonElement;

public interface IJsonSaveLoad<T extends JsonElement>
{
	public T writeToJson(T json, EnumSaveType saveType);
	public void readFromJson(T json, EnumSaveType saveType);
}
