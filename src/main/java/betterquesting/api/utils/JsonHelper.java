package betterquesting.api.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import betterquesting.api.ExpansionAPI;
import com.google.gson.JsonArray;
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
		
		if(json.has(id) && json.get(id).isJsonPrimitive() && json.get(id).getAsJsonPrimitive().isNumber())
		{
			return json.get(id).getAsInt();
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
			try // Booleans can be stored as strings so there is no quick way of determining whether it is valid or not
			{
				return json.get(id).getAsBoolean();
			} catch(Exception e)
			{
				JsonPrimitive prim = new JsonPrimitive(def);
				json.add(id, prim);
				return def;
			}
		} else
		{
			return def;
		}
	}
	
	public static boolean isItem(JsonObject json)
	{
		return json != null && json.has("id") && json.has("Count") && json.has("Damage") && Item.itemRegistry.containsKey(GetString(json, "id", ""));
	}
	
	public static boolean isFluid(JsonObject json)
	{
		return json != null && json.has("FluidName") && json.has("Amount") && FluidRegistry.getFluid(GetString(json, "FluidName", "")) != null;
	}
	
	public static boolean isEntity(JsonObject json)
	{
		return json != null && json.has("id") && EntityList.stringToClassMapping.containsKey(GetString(json, "id", ""));
	}
	
	/**
	 * Converts a JsonObject to an ItemStack. May return a placeholder if the correct mods are not installed</br>
	 * This should be the standard way to load items into quests in order to retain all potential data
	 */
	public static BigItemStack JsonToItemStack(JsonObject json)
	{
		if(json == null || !json.has("id") || !json.get("id").isJsonPrimitive())
		{
			return new BigItemStack(Blocks.stone);
		}
		
		JsonPrimitive jID = json.get("id").getAsJsonPrimitive();
		int count = JsonHelper.GetNumber(json, "Count", 1).intValue();
		String oreDict = JsonHelper.GetString(json, "OreDict", "");
		int damage = JsonHelper.GetNumber(json, "Damage", OreDictionary.WILDCARD_VALUE).intValue();
		damage = damage >= 0? damage : OreDictionary.WILDCARD_VALUE;
		
		Item item;
		
		if(jID.isNumber())
		{
			item = (Item)Item.itemRegistry.getObjectById(jID.getAsInt()); // Old format (numbers)
		} else
		{
			item = (Item)Item.itemRegistry.getObject(jID.getAsString()); // New format (names)
		}
		
		NBTTagCompound tags = null;
		if(json.has("tag"))
		{
			tags = NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "tag"), new NBTTagCompound(), true);
		}
		
		return ExpansionAPI.INSTANCE.getPlaceholderMaker().convertPlaceholder(item, jID.getAsString(), count, damage, oreDict, tags);
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
		
		json.addProperty("id", Item.itemRegistry.getNameForObject(stack.getBaseStack().getItem()));
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
		
		return ExpansionAPI.INSTANCE.getPlaceholderMaker().convertPlaceholder(fluid, name, amount, tags);
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
		
		if(tags.hasKey("id") && EntityList.stringToClassMapping.containsKey(tags.getString("id")))
		{
			entity = EntityList.createEntityFromNBT(tags, world);
		}
		
		return ExpansionAPI.INSTANCE.getPlaceholderMaker().convertPlaceholder(entity, world, tags);
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
