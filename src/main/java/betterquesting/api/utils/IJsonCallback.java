package betterquesting.api.utils;

import com.google.gson.JsonElement;

public interface IJsonCallback<T extends JsonElement>
{
	public void setJson(int id, T json);
}
