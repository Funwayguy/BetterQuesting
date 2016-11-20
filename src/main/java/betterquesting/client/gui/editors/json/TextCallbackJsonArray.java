package betterquesting.client.gui.editors.json;

import java.util.ArrayList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import betterquesting.api.other.ITextCallback;
import betterquesting.api.utils.JsonHelper;

public class TextCallbackJsonArray implements ITextCallback
{
	private final JsonArray json;
	private final int index;
	
	public TextCallbackJsonArray(JsonArray json, int index)
	{
		this.json = json;
		this.index = index;
	}
	
	@Override
	public void setText(String text)
	{
		ArrayList<JsonElement> list = JsonHelper.GetUnderlyingArray(json);
		
		if(list == null || index < 0 || index >= list.size())
		{
			return;
		}
		
		list.set(index, new JsonPrimitive(text));
	}
}
