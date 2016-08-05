package betterquesting.api.utils;

import com.google.gson.JsonElement;

/**
 * Useful for passing restricted access to child GUIs or attaching data to buttons
 */
public interface IJsonStorage<T extends JsonElement>
{
	public T getJson();
}
