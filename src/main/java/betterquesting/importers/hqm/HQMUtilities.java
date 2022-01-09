package betterquesting.importers.hqm;

import betterquesting.api.placeholders.PlaceholderConverter;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.core.BetterQuesting;
import betterquesting.importers.hqm.converters.items.HQMItem;
import betterquesting.importers.hqm.converters.items.HQMItemBag;
import betterquesting.importers.hqm.converters.items.HQMItemHeart;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;

import java.util.HashMap;

public class HQMUtilities
{
	/**
	 * Get HQM formatted item, Type 1
	 */
	public static BigItemStack HQMStackT1(JsonObject json) // This can return multiple stacks in the event the stack size exceeds 127
	{
		String iID = JsonHelper.GetString(json, "id", "minecraft:stone");
		Item item = Item.REGISTRY.getObject(new ResourceLocation(iID));
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
				
				tags = JsonToNBT.getTagFromJson(rawNbt);
			} catch(Exception e)
			{
                BetterQuesting.logger.log(Level.ERROR, "Unable to convert HQM NBT data. This is likely a HQM Gson/Json formatting issue", e);
			}
		}
		
		HQMItem hqm = itemConverters.get(iID.toLowerCase());
		if(hqm != null) return hqm.convertItem(damage, amount, tags);
		
		return PlaceholderConverter.convertItem(item, iID, amount, damage, "", tags);
	}
	
	/**
	 * Get HQM formatted item, Type 2
	 */
	public static BigItemStack HQMStackT2(JsonObject rJson) // This can return multiple stacks in the event the stack size exceeds 127
	{
		JsonObject json = JsonHelper.GetObject(rJson, "item");
		String iID = JsonHelper.GetString(json, "id", "minecraft:stone");
		Item item = Item.REGISTRY.getObject(new ResourceLocation(iID));
		int amount = JsonHelper.GetNumber(rJson, "required", 1).intValue();
		int damage = JsonHelper.GetNumber(json, "damage", 0).intValue();
		boolean oreDict = JsonHelper.GetString(rJson, "precision", "").equalsIgnoreCase("ORE_DICTIONARY");
		
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
				
				tags = JsonToNBT.getTagFromJson(rawNbt);
			} catch(Exception e)
			{
                BetterQuesting.logger.log(Level.ERROR, "Unable to convert HQM NBT data. This is likely a HQM Gson/Json formatting issue", e);
			}
		}
		
		HQMItem hqm = itemConverters.get(iID.toLowerCase());
		if(hqm != null) return hqm.convertItem(damage, amount, tags);
		
		BigItemStack stack = PlaceholderConverter.convertItem(item, iID, amount, damage, "", tags);
		
		if(oreDict && item != null)
		{
			int[] oreId = OreDictionary.getOreIDs(stack.getBaseStack());
			if(oreId.length > 0) stack.setOreDict(OreDictionary.getOreName(oreId[0]));
		}
		
		return stack;
	}
	
	public static FluidStack HQMStackT3(JsonObject json)
	{
		String name = JsonHelper.GetString(json, "fluid", "water");
		Fluid fluid = FluidRegistry.getFluid(name);
		int amount = JsonHelper.GetNumber(json, "required", 1000).intValue();
        
        return PlaceholderConverter.convertFluid(fluid, name, amount, null);
	}
	
	private static HashMap<String,HQMItem> itemConverters = new HashMap<>();
	
	static
	{
		itemConverters.put("hardcorequesting:hearts", new HQMItemHeart());
		itemConverters.put("hardcorequesting:bags", new HQMItemBag());
	}
}
