package betterquesting.utils;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Helper class for comparing ItemStacks in quests
 */
public class ItemComparison
{
    /**
     * Check whether two stacks match with optional NBT checks
     * @param stack1
     * @param stack2
     * @return
     */
    public static boolean StackMatch(ItemStack stack1, ItemStack stack2, boolean nbtCheck)
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
    		if(stack1.getTagCompound() != null && stack1.getTagCompound() != null)
    		{
    			if(!stack1.getTagCompound().equals(stack2.getTagCompound()))
    			{
    				return false;
    			}
    		} else if((stack1.getTagCompound() == null || stack2.getTagCompound() == null) && stack1.getTagCompound() != stack2.getTagCompound())
			{
				return false; // One of these stacks is missing tags that the other has!
			}
			
			// Well either the tags match or neither stack has any tags at this point
    	}
    	
    	return stack1.getItem() == stack2.getItem() && (stack1.getItemDamage() == stack2.getItemDamage() || stack1.getItem().isDamageable() || stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE);
    }
    
    /**
     * Check if the item stack is part of the ore dictionary listing with the given name (NBT ignored)
     * @param stack
     * @param name
     * @return
     */
    public static boolean OreDictionaryMatch(ItemStack stack, String name)
    {
    	for(ItemStack oreStack : OreDictionary.getOres(name))
    	{
    		if(StackMatch(stack, oreStack, false))
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
    	if(StackMatch(stack1, stack2, false))
    	{
    		return true;
    	}
    	
    	for(int id : OreDictionary.getOreIDs(stack1)) // Search all ore dictionary listings for matches
    	{
    		if(OreDictionaryMatch(stack2, OreDictionary.getOreName(id)))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
}
