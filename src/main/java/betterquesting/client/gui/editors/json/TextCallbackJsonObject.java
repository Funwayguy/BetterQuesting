package betterquesting.client.gui.editors.json;

import betterquesting.api.other.ITextCallback;
import com.google.gson.JsonObject;

public class TextCallbackJsonObject implements ITextCallback
{
	private final JsonObject json;
	private final String key;
	
	public TextCallbackJsonObject(JsonObject json, String key)
	{
		this.json = json;
		this.key = key;
	}
	
	@Override
	public void setText(String text)
	{
		this.json.addProperty(key, text);
	}
}
