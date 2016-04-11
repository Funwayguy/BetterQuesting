package betterquesting.utils;

import java.util.ArrayList;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Helper class for comparing ItemStacks in quests
 */
public class ItemComparison
{
    /**
     * Check whether two stacks match with optional NBT and Ore Dictionary checks
     * @param stack1
     * @param stack2
     * @return
     */
    public static boolean StackMatch(ItemStack stack1, ItemStack stack2, boolean nbtCheck, boolean partialNBT)
    {
    	// Some quick null checks
    	if(stack1 == null && stack2 == null)
    	{
    		return true;
    	} else if(stack1 == null || stack2 == null)
    	{
    		return false;
    	}
    	
    	if(nbtCheck)
    	{
    		if(stack1.getTagCompound() != null && stack2.getTagCompound() != null)
    		{
    			if(!CompareNBTTag(stack1.getTagCompound(), stack2.getTagCompound(), partialNBT))
    			{
    				return false;
    			}
    		} else if(stack1.getTagCompound() != null)
			{
				return false; // One of these stacks is missing tags that the other has!
			}
			
			// Well either the tags match or neither stack has any tags at this point
    	}
    	
    	return stack1.getItem() == stack2.getItem() && (stack1.getItemDamage() == stack2.getItemDamage() || stack1.getItem().isDamageable() || stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE);
    }
    
    public static boolean CompareNBTTag(NBTBase tag1, NBTBase tag2, boolean partial)
    {
    	if((tag1 == null && tag2 != null) || (tag1 != null && tag2 == null))
    	{
    		return false;
    	} else if(tag1 == null && tag2 == null)
    	{
    		return true;
    	}
    	
    	if(tag1 instanceof NBTTagCompound && tag2 instanceof NBTTagCompound)
    	{
    		return CompareNBTTagCompound((NBTTagCompound)tag1, (NBTTagCompound)tag2, partial);
    	} else if(tag1 instanceof NBTTagList && tag2 instanceof NBTTagList)
    	{
    		NBTTagList list1 = (NBTTagList)tag1;
    		NBTTagList list2 = (NBTTagList)tag2;
    		
    		if(list1.tagCount() > list2.tagCount() || (!partial && list1.tagCount() != list2.tagCount()))
    		{
    			return false; // Sample is missing requested tags or is not exact
    		}
    		
    		topLoop:
    		for(int i = 0; i < list1.tagCount(); i++)
    		{
    			NBTBase lt1 = list1.getCompoundTagAt(i);
    			
    			for(int j = 0; j < list2.tagCount(); j++)
    			{
    				if(CompareNBTTag(lt1, list2.getCompoundTagAt(j), partial))
    				{
    					continue topLoop;
    				}
    			}
    			
    			return false; // Couldn't find requested tag in list
    		}
    	} else if(tag1 instanceof NBTTagIntArray && tag2 instanceof NBTTagIntArray)
    	{
    		NBTTagIntArray list1 = (NBTTagIntArray)tag1;
    		NBTTagIntArray list2 = (NBTTagIntArray)tag2;
    		
    		if(list1.func_150302_c().length > list2.func_150302_c().length || (!partial && list1.func_150302_c().length != list2.func_150302_c().length))
    		{
    			return false; // Sample is missing requested tags or is not exact
    		}
    		
    		topLoop:
    		for(int i = 0; i < list1.func_150302_c().length; i++)
    		{
    			for(int j = 0; j < list2.func_150302_c().length; j++)
    			{
    				if(list1.func_150302_c()[i] == list2.func_150302_c()[j])
    				{
    					continue topLoop;
    				}
    			}
    			
    			return false; // Couldn't find requested integer in list
    		}
    		
    		return false;
    	} else if(tag1 instanceof NBTTagByteArray && tag2 instanceof NBTTagByteArray)
    	{
    		NBTTagByteArray list1 = (NBTTagByteArray)tag1;
    		NBTTagByteArray list2 = (NBTTagByteArray)tag2;
    		
    		if(list1.func_150292_c().length > list2.func_150292_c().length || (!partial && list1.func_150292_c().length != list2.func_150292_c().length))
    		{
    			return false; // Sample is missing requested tags or is not exact
    		}
    		
    		ArrayList<Integer> usedIdxs = new ArrayList<Integer>(); // Duplicate control
    		
    		topLoop:
    		for(int i = 0; i < list1.func_150292_c().length; i++)
    		{
    			for(int j = 0; j < list2.func_150292_c().length; j++)
    			{
    				if(usedIdxs.contains(j))
    				{
    					continue;
    				} else if(list1.func_150292_c()[i] == list2.func_150292_c()[j])
    				{
    					usedIdxs.add(j);
    					continue topLoop;
    				}
    			}
    			
    			return false; // Couldn't find requested integer in list
    		}
    		
    		return false;
    	} else if(tag1 instanceof NBTTagString && tag2 instanceof NBTTagString)
    	{
    		return tag1.equals(tag2);
    	} else if(tag1 instanceof NBTPrimitive && tag2 instanceof NBTPrimitive) // Standardize numbers to not care about format
    	{
    		Number num1 = NBTConverter.getNumber(tag1);
    		Number num2 = NBTConverter.getNumber(tag2);
    		
    		// Second number will be cast to the requested number format
    		if(tag1 instanceof NBTTagFloat || tag1 instanceof NBTTagDouble)
    		{
    			return num1.doubleValue() == num2.doubleValue();
    		} else
    		{
    			return num1.longValue() == num2.longValue();
    		}
    	} else
    	{
    		return tag1.equals(tag2);
    	}
    	
    	return true;
    }
    
    @SuppressWarnings("unchecked")
	private static boolean CompareNBTTagCompound(NBTTagCompound reqTags, NBTTagCompound sample, boolean partial)
    {
    	for(String key : (Set<String>)reqTags.func_150296_c())
    	{
    		if(!sample.hasKey(key))
    		{
    			return false;
    		} else if(!CompareNBTTag(reqTags.getTag(key), sample.getTag(key), partial))
    		{
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    public static boolean OreDictionaryMatch(String name, ItemStack stack)
    {
    	return OreDictionaryMatch(name, new NBTTagCompound(), stack, false, false);
    }
    
    /**
     * Check if the item stack is part of the ore dictionary listing with the given name (NBT ignored)
     * @param stack
     * @param name
     * @return
     */
    public static boolean OreDictionaryMatch(String name, NBTTagCompound tags, ItemStack stack, boolean nbtCheck, boolean partialNBT)
    {
    	for(ItemStack oreStack : OreDictionary.getOres(name))
    	{
    		ItemStack tmp = oreStack.copy();
    		tmp.setTagCompound(tags);
    		
    		if(StackMatch(tmp, stack, nbtCheck, partialNBT))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    /**
     * Check if the two stacks match directly or through ore dictionary listings (NBT ignored)
     * @param stack1
     * @param stack2
     * @return
     */
    public static boolean AllMatch(ItemStack stack1, ItemStack stack2)
    {
    	if(StackMatch(stack1, stack2, false, false))
    	{
    		return true;
    	}
    	
    	for(int id : OreDictionary.getOreIDs(stack1)) // Search all ore dictionary listings for matches
    	{
    		if(OreDictionaryMatch(OreDictionary.getOreName(id), stack2))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
}
