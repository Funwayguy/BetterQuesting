package betterquesting.client.gui.editors.json;

import betterquesting.api.misc.ICallback;
import com.google.gson.JsonObject;

public class TextCallbackJsonObject implements ICallback<String>
{
	private final JsonObject json;
	private final String key;
	
	public TextCallbackJsonObject(JsonObject json, String key)
	{
		this.json = json;
		this.key = key;
	}
	
	@Override
	public void setValue(String text)
	{
		this.json.addProperty(key, text);
	}
}
