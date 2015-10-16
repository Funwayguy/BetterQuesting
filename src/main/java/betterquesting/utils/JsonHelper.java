package betterquesting.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Used to read JSON data with pre-made checks for null entries and casting.</br>
 * In the event the requested value is missing, it will be added to the JSON object
 */
public class JsonHelper
{
	public static JsonArray GetArray(JsonObject json, String id)
	{
		if(json.has(id) && json.get(id) instanceof JsonArray)
		{
			return json.get(id).getAsJsonArray();
		} else
		{
			JsonArray array = new JsonArray();
			json.add(id, array);
			return array;
		}
	}
	
	public static JsonObject GetObject(JsonObject json, String id)
	{
		if(json.has(id) && json.get(id) instanceof JsonObject)
		{
			return json.get(id).getAsJsonObject();
		} else
		{
			JsonObject obj = new JsonObject();
			json.add(id, obj);
			return obj;
		}
	}
	
	public static String GetString(JsonObject json, String id, String def)
	{
		if(json.has(id) && json.get(id) instanceof JsonPrimitive)
		{
			return json.get(id).getAsString();
		} else
		{
			JsonPrimitive prim = new JsonPrimitive(def);
			json.add(id, prim);
			return def;
		}
	}
	
	public static Number GetNumber(JsonObject json, String id, Number def)
	{
		if(json.has(id) && json.get(id) instanceof JsonPrimitive)
		{
			return json.get(id).getAsInt();
		} else
		{
			JsonPrimitive prim = new JsonPrimitive(def);
			json.add(id, prim);
			return def;
		}
	}
	
	public static boolean GetBoolean(JsonObject json, String id, boolean def)
	{
		if(json.has(id) && json.get(id) instanceof JsonPrimitive)
		{
			return json.get(id).getAsBoolean();
		} else
		{
			JsonPrimitive prim = new JsonPrimitive(def);
			json.add(id, prim);
			return def;
		}
	}
}
