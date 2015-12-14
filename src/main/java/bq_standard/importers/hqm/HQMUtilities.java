package bq_standard.importers.hqm;

import java.util.HashMap;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import bq_standard.core.BQ_Standard;
import bq_standard.importers.hqm.converters.items.HQMItem;
import bq_standard.importers.hqm.converters.items.HQMItemBag;
import bq_standard.importers.hqm.converters.items.HQMItemHeart;
import com.google.gson.JsonObject;

public class HQMUtilities
{
	/**
	 * Get HQM formatted item, Type 1
	 */
	public static BigItemStack HQMStackT1(JsonObject json) // This can return multiple stacks in the event the stack size exceeds 127
	{
		String iID = JsonHelper.GetString(json, "id", "minecraft:stone");
		Item item = (Item)Item.itemRegistry.getObject(iID);
		int amount = JsonHelper.GetNumber(json, "amount", 1).intValue();
		int damage = JsonHelper.GetNumber(json, "damage", 0).intValue();
		NBTTagCompound tags = null;
		
		if(json.has("nbt"))
		{
			try
			{
				String rawNbt = json.get("nbt").toString(); // Must use this method. Gson formatting will damage it otherwise
				
				// Hack job to fix backslashes (why are 2 Json formats being used in HQM?!)
				rawNbt = rawNbt.replaceFirst("\"", ""); // Delete first quote
				rawNbt = rawNbt.substring(0, rawNbt.length() - 1); // Delete last quote
				rawNbt = rawNbt.replace(":\\\"", ":\""); // Fix start of strings
				rawNbt = rawNbt.replace("\\\",", "\","); // Fix middle of lists
				rawNbt = rawNbt.replace("\\\"}", "\"}"); // Fix end of strings
				rawNbt = rawNbt.replace("\\\"]", "\"]"); // Fix end of lists
				rawNbt = rawNbt.replace("[\\\"", "[\""); // Fix start of lists
				rawNbt = rawNbt.replace("\\n", "\n");
				
				NBTBase nbt = JsonToNBT.func_150315_a(rawNbt);
				
				if(nbt != null && nbt instanceof NBTTagCompound)
				{
					tags = (NBTTagCompound)nbt;
				}
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to convert HQM NBT data. This is likely a HQM Gson/Json formatting issue", e);
			}
		}
		
		HQMItem hqm = itemConverters.get(iID);
		
		if(hqm != null)
		{
			return hqm.convertItem(damage, amount, tags);
		} else if(item == null)
		{
			item = BetterQuesting.placeholder;
			NBTTagCompound tmp = new NBTTagCompound();
			if(tags != null)
			{
				tmp.setTag("orig_tag", tags);
			}
			tmp.setString("orig_id", iID);
			tags = tmp;
		}
		
		BigItemStack stack = new BigItemStack(item, amount, damage);
		
		if(tags != null)
		{
			stack.SetTagCompound(tags);
		}
		
		return stack;
	}
	
	/**
	 * Get HQM formatted item, Type 2
	 */
	public static BigItemStack HQMStackT2(JsonObject rJson) // This can return multiple stacks in the event the stack size exceeds 127
	{
		JsonObject json = JsonHelper.GetObject(rJson, "item");
		String iID = JsonHelper.GetString(json, "id", "minecraft:stone");
		Item item = (Item)Item.itemRegistry.getObject(iID);
		int amount = JsonHelper.GetNumber(rJson, "required", 1).intValue();
		int damage = JsonHelper.GetNumber(json, "damage", 0).intValue();
		NBTTagCompound tags = null;
		
		if(json.has("nbt"))
		{
			try
			{
				String rawNbt = json.get("nbt").toString(); // Must use this method. Gson formatting will damage it otherwise
				
				// Hack job to fix backslashes (why are 2 Json formats being used in HQM?!)
				rawNbt = rawNbt.replaceFirst("\"", ""); // Delete first quote
				rawNbt = rawNbt.substring(0, rawNbt.length() - 1); // Delete last quote
				rawNbt = rawNbt.replace(":\\\"", ":\""); // Fix start of strings
				rawNbt = rawNbt.replace("\\\",", "\","); // Fix middle of lists
				rawNbt = rawNbt.replace("\\\"}", "\"}"); // Fix end of strings
				rawNbt = rawNbt.replace("\\\"]", "\"]"); // Fix end of lists
				rawNbt = rawNbt.replace("[\\\"", "[\""); // Fix start of lists
				rawNbt = rawNbt.replace("\\n", "\n");
				
				NBTBase nbt = JsonToNBT.func_150315_a(rawNbt);
				
				if(nbt != null && nbt instanceof NBTTagCompound)
				{
					tags = (NBTTagCompound)nbt;
				}
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to convert HQM NBT data. This is likely a HQM Gson/Json formatting issue", e);
			}
		}
		
		HQMItem hqm = itemConverters.get(iID);
		
		if(hqm != null)
		{
			return hqm.convertItem(damage, amount, tags);
		} else if(item == null)
		{
			item = BetterQuesting.placeholder;
			NBTTagCompound tmp = new NBTTagCompound();
			if(tags != null)
			{
				tmp.setTag("orig_tag", tags);
			}
			tmp.setString("orig_id", iID);
			tags = tmp;
		}
		
		BigItemStack stack = new BigItemStack(item, amount, damage);
		
		if(tags != null)
		{
			stack.SetTagCompound(tags);
		}
		
		return stack;
	}
	
	public static FluidStack HQMStackT3(JsonObject json)
	{
		String name = JsonHelper.GetString(json, "fluid", "water");
		Fluid fluid = FluidRegistry.getFluid(name);
		int amount = JsonHelper.GetNumber(json, "required", 1000).intValue();
		
		if(fluid == null)
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setString("orig_id", name);
			FluidStack stack = new FluidStack(BetterQuesting.fluidPlaceholder, amount);
			stack.tag = tags;
			return stack;
		}
		
		return new FluidStack(fluid, amount);
	}
	
	static HashMap<String,HQMItem> itemConverters = new HashMap<String,HQMItem>();
	
	static
	{
		itemConverters.put("HardcoreQuesting:hearts", new HQMItemHeart());
		itemConverters.put("HardcoreQuesting:bags", new HQMItemBag());
	}
}
