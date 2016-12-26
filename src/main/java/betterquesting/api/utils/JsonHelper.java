package betterquesting.api.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.placeholders.PlaceholderConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Used to read JSON data with pre-made checks for null entries and casting.
 * Missing entries will return a default/blank value instead of null without
 * editing the parent JSON.<br>
 * In the event the requested item, fluid or entity is missing, a place holder will be substituted
 */
public class JsonHelper
{
	public static JsonArray GetArray(JsonObject json, String id)
	{
		if(json == null)
		{
			return new JsonArray();
		}
		
		if(json.has(id) && json.get(id).isJsonArray())
		{
			return json.get(id).getAsJsonArray();
		} else
		{
			return new JsonArray();
		}
	}
	
	public static JsonObject GetObject(JsonObject json, String id)
	{
		if(json == null)
		{
			return new JsonObject();
		}
		
		if(json.has(id) && json.get(id).isJsonObject())
		{
			return json.get(id).getAsJsonObject();
		} else
		{
			return new JsonObject();
		}
	}
	
	public static String GetString(JsonObject json, String id, String def)
	{
		if(json == null)
		{
			return def;
		}
		
		if(json.has(id) && json.get(id).isJsonPrimitive() && json.get(id).getAsJsonPrimitive().isString())
		{
			return json.get(id).getAsString();
		} else
		{
			return def;
		}
	}
	
	public static Number GetNumber(JsonObject json, String id, Number def)
	{
		if(json == null)
		{
			return def;
		}
		
		if(json.has(id) && json.get(id).isJsonPrimitive())
		{
			try
			{
				return json.get(id).getAsNumber();
			} catch(Exception e)
			{
				return def;
			}
		} else
		{
			return def;
		}
	}
	
	public static boolean GetBoolean(JsonObject json, String id, boolean def)
	{
		if(json == null)
		{
			return def;
		}
		
		if(json.has(id) && json.get(id).isJsonPrimitive())
		{
			try
			{
				return json.get(id).getAsBoolean();
			} catch(Exception e)
			{
				return def;
			}
		} else
		{
			return def;
		}
	}
	
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
			QuestingAPI.getLogger().log(Level.ERROR, "Unable to retrieve underlying JsonArray:", e);
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
			InputStreamReader fr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
			JsonObject json = new Gson().fromJson(fr, JsonObject.class);
			fr.close();
			return json;
		} catch(Exception e)
		{
			QuestingAPI.getLogger().log(Level.ERROR, "An error occured while loading JSON from file:", e);
			
			int i = 0;
			File bkup = new File(file.getParent(), "malformed_" + file.getName() + i + ".json");
			
			while(bkup.exists())
			{
				i++;
				bkup = new File(file.getParent(), "malformed_" + file.getName() + i + ".json");
			}
			
			QuestingAPI.getLogger().log(Level.ERROR, "Creating backup at: " + bkup.getAbsolutePath());
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
			
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			new GsonBuilder().setPrettyPrinting().create().toJson(jObj, fw);
			fw.close();
		} catch(Exception e)
		{
			QuestingAPI.getLogger().log(Level.ERROR, "An error occured while saving JSON to file:", e);
			return;
		}
	}
	
	public static void CopyPaste(File fileIn, File fileOut)
	{
		//final int bufferSize = 0x100000;
		
		BufferedReader fr = null;
		BufferedWriter fw = null;
		
		try
		{
			fr = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn), StandardCharsets.UTF_8));
			fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut), StandardCharsets.UTF_8));
			
			char[] buffer = new char[256];
			int read;
			while((read = fr.read(buffer)) != -1)
			{
				fw.write(buffer, 0, read);
			}
		} catch(Exception e1)
		{
			QuestingAPI.getLogger().log(Level.ERROR, "Failed copy paste", e1);
		} finally
		{
			try
			{
				fr.close();
				fw.close();
			} catch(Exception e2){}
		}
	}
	
	public static boolean isItem(JsonObject json)
	{
		if(json != null && json.has("id") && json.has("Count") && json.has("Damage") && json.get("id").isJsonPrimitive())
		{
			if(!json.get("id").getAsJsonPrimitive().isNumber())
			{
				 return Item.REGISTRY.containsKey(new ResourceLocation(json.get("id").getAsString()));
			} else
			{
				return Item.REGISTRY.getObjectById(json.get("id").getAsNumber().intValue()) != null;
			}
		}
		
		return false;
	}
	
	public static boolean isFluid(JsonObject json)
	{
		return json != null && json.has("FluidName") && json.has("Amount") && FluidRegistry.getFluid(GetString(json, "FluidName", "")) != null;
	}
	
	public static boolean isEntity(JsonObject json)
	{
		NBTTagCompound tags = NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound(), true);
		return tags.hasKey("id") && EntityList.NAME_TO_CLASS.containsKey(tags.getString("id"));
	}
	
	/**
	 * Converts a JsonObject to an ItemStack. May return a placeholder if the correct mods are not installed</br>
	 * This should be the standard way to load items into quests in order to retain all potential data
	 */
	public static BigItemStack JsonToItemStack(JsonObject json)
	{
		if(json == null || !json.has("id") || !json.get("id").isJsonPrimitive())
		{
			return new BigItemStack(Blocks.STONE);
		}
		
		JsonPrimitive jID = json.get("id").getAsJsonPrimitive();
		int count = JsonHelper.GetNumber(json, "Count", 1).intValue();
		String oreDict = JsonHelper.GetString(json, "OreDict", "");
		int damage = JsonHelper.GetNumber(json, "Damage", OreDictionary.WILDCARD_VALUE).intValue();
		damage = damage >= 0? damage : OreDictionary.WILDCARD_VALUE;
		
		Item item;
		
		if(jID.isNumber())
		{
			item = (Item)Item.REGISTRY.getObjectById(jID.getAsInt()); // Old format (numbers)
		} else
		{
			item = (Item)Item.REGISTRY.getObject(new ResourceLocation(jID.getAsString())); // New format (names)
		}
		
		NBTTagCompound tags = null;
		if(json.has("tag"))
		{
			tags = NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "tag"), new NBTTagCompound(), true);
		}
		
		return PlaceholderConverter.convertItem(item, jID.getAsString(), count, damage, oreDict, tags);
	}
	
	/**
	 * Use this for quests instead of converter NBT because this doesn't use ID numbers
	 */
	public static JsonObject ItemStackToJson(BigItemStack stack, JsonObject json)
	{
		if(stack == null)
		{
			return json;
		}
		
		json.addProperty("id", Item.REGISTRY.getNameForObject(stack.getBaseStack().getItem()).toString());
		json.addProperty("Count", stack.stackSize);
		json.addProperty("OreDict", stack.oreDict);
		json.addProperty("Damage", stack.getBaseStack().getItemDamage());
		if(stack.HasTagCompound())
		{
			json.add("tag", NBTConverter.NBTtoJSON_Compound(stack.GetTagCompound(), new JsonObject(), true));
		}
		return json;
	}
	
	public static FluidStack JsonToFluidStack(JsonObject json)
	{
		String name = GetString(json, "FluidName", "water");
		int amount = GetNumber(json, "Amount", 1000).intValue();
		NBTTagCompound tags = null;
		
		if(json.has("Tag"))
		{
			tags = NBTConverter.JSONtoNBT_Object(GetObject(json, "Tag"), new NBTTagCompound(), true);
		}
		
		Fluid fluid = FluidRegistry.getFluid(name);
		
		return PlaceholderConverter.convertFluid(fluid, name, amount, tags);
	}
	
	public static JsonObject FluidStackToJson(FluidStack stack, JsonObject json)
	{
		if(stack == null)
		{
			return json;
		}
		
		json.addProperty("FluidName", FluidRegistry.getFluidName(stack));
		json.addProperty("Amount", stack.amount);
		if(stack.tag != null)
		{
			json.add("Tag", NBTConverter.NBTtoJSON_Compound(stack.tag, new JsonObject(), true));
		}
		return json;
	}
	
	public static Entity JsonToEntity(JsonObject json, World world)
	{
		return JsonToEntity(json, world, true);
	}
	
	// Extra option to allow null returns for checking purposes
	public static Entity JsonToEntity(JsonObject json, World world, boolean allowPlaceholder)
	{
		NBTTagCompound tags = NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound(), true);
		Entity entity = null;
		
		if(tags.hasKey("id") && EntityList.NAME_TO_CLASS.containsKey(tags.getString("id")))
		{
			entity = EntityList.createEntityFromNBT(tags, world);
		}
		
		return PlaceholderConverter.convertEntity(entity, world, tags);
	}
	
	public static JsonObject EntityToJson(Entity entity, JsonObject json)
	{
		if(entity == null)
		{
			return json;
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		entity.writeToNBTOptional(tags);
		String id = EntityList.getEntityString(entity);
		tags.setString("id", id); // Some entities don't write this to file in certain cases
		NBTConverter.NBTtoJSON_Compound(tags, json, true);
		return json;
	}
}
