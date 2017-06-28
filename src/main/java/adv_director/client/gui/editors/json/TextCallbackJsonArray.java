package adv_director.client.gui.editors.json;

import java.util.ArrayList;
import adv_director.api.misc.ICallback;
import adv_director.api.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class TextCallbackJsonArray implements ICallback<String>
{
	private final JsonArray json;
	private final int index;
	
	public TextCallbackJsonArray(JsonArray json, int index)
	{
		this.json = json;
		this.index = index;
	}
	
	@Override
	public void setValue(String text)
	{
		ArrayList<JsonElement> list = JsonHelper.GetUnderlyingArray(json);
		
		if(list == null || index < 0 || index >= list.size())
		{
			return;
		}
		
		list.set(index, new JsonPrimitive(text));
	}
}
