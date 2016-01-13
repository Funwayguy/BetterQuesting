package betterquesting.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * IO methods for reading/writing JsonObjects to/from files plus other manipulation methods
 */
public class JsonIO
{
	@SuppressWarnings("unchecked")
	public static ArrayList<JsonElement> GetUnderlyingArray(JsonArray array)
	{
		try
		{
			Field field = JsonArray.class.getDeclaredField("elements");
			field.setAccessible(true);
			
			return (ArrayList<JsonElement>)field.get(array);
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to retrieve underlying JsonArray:", e);
		}
		
		return null;
	}
	
	public static JsonObject ReadFromFile(File file)
	{
		try
		{
			FileReader fr = new FileReader(file);
			JsonObject json = new Gson().fromJson(fr, JsonObject.class);
			fr.close();
			return json;
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while loading JSON from file:", e);
			return new JsonObject(); // Just a safety measure against NPEs
		}
	}
	
	public static void WriteToFile(File file, JsonObject jObj)
	{
		try
		{
			if(!file.exists())
			{
				if(file.getParentFile() != null)
				{
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file);
			new GsonBuilder().setPrettyPrinting().create().toJson(jObj, fw);
			fw.close();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while saving JSON to file:", e);
			return;
		}
	}
}
