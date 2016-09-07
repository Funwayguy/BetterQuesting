package betterquesting.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
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
		if(file == null || !file.exists())
		{
			return new JsonObject();
		}
		
		try
		{
			//FileReader fr = new FileReader(file);
			Reader fr = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
			JsonObject json = new Gson().fromJson(fr, JsonObject.class);
			fr.close();
			return json;
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while loading JSON from file:", e);
			
			int i = 0;
			File bkup = new File(file.getParent(), "malformed_" + file.getName() + i + ".json");
			
			while(bkup.exists())
			{
				i++;
				bkup = new File(file.getParent(), "malformed_" + file.getName() + i + ".json");
			}
			
			BetterQuesting.logger.log(Level.ERROR, "Creating backup at: " + bkup.getAbsolutePath());
			CopyPaste(file, bkup);
			
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
			
			//FileWriter fw = new FileWriter(file);
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file),Charset.forName("UTF-8"));
			new GsonBuilder().setPrettyPrinting().create().toJson(jObj, fw);
			fw.close();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while saving JSON to file:", e);
			return;
		}
	}
	
	public static void CopyPaste(File fileIn, File fileOut)
	{
		try
		{
			FileReader fr = new FileReader(fileIn);
			FileWriter fw = new FileWriter(fileOut);
			
			char[] buf = new char[256];
			while(fr.ready())
			{
				fr.read(buf);
				fw.write(buf);
			}
			
			fr.close();
			fw.close();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Failed copy paste", e);
		}
	}
}
