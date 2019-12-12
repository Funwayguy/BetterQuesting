package betterquesting.api.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Helper class for comparing ItemStacks in quests
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ItemComparison
{
    /**
     * Check whether two stacks match with optional NBT and Ore Dictionary checks
     */
    public static boolean StackMatch(ItemStack stack1, ItemStack stack2, boolean nbtCheck, boolean partialNBT)
    {
    	// Some quick checks
    	if(stack1 == stack2)
    	{
    		return true;
    	} else if(stack1 == null || stack2 == null)
    	{
    		return false;
    	}
    	
    	if(nbtCheck)
    	{
    		if(!partialNBT && !stack1.areCapsCompatible(stack2))
    		{
    			return false;
    		} else if(!CompareNBTTag(stack1.getTag(), stack2.getTag(), partialNBT))
    		{
    			return false;
    		}
    	}
    	
    	return stack1.getItem() == stack2.getItem();// && (stack1.getItemDamage() == stack2.getItemDamage() || stack1.getItem().isDamageable() || stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE);
    }
    
    public static boolean CompareNBTTag(INBT tag1, INBT tag2, boolean partial)
    {
    	if(isEmptyNBT(tag1) != isEmptyNBT(tag2)) // One is null, the other is not
    	{
    		return false;
    	} else if(isEmptyNBT(tag1)) // The opposing tag will always be null at this point if the other already is
    	{
    		return true;
    	} else if(!(tag1 instanceof NumberNBT && tag2 instanceof NumberNBT) && tag1.getId() != tag2.getId()) return false; // Incompatible tag types (and not a numbers we can cast)
    	
    	if(tag1 instanceof CompoundNBT && tag2 instanceof CompoundNBT)
    	{
    		return CompareNBTTagCompound((CompoundNBT)tag1, (CompoundNBT)tag2, partial);
    	} else if(tag1 instanceof ListNBT && tag2 instanceof ListNBT)
    	{
    		ListNBT list1 = (ListNBT)tag1;
    		ListNBT list2 = (ListNBT)tag2;
    		
    		if(list1.size() > list2.size() || (!partial && list1.size() != list2.size()))
    		{
    			return false; // Sample is missing requested tags or is not exact
    		}
    		
    		topLoop:
    		for(int i = 0; i < list1.size(); i++)
    		{
    			INBT lt1 = list1.get(i);
    			
    			for(int j = 0; j < list2.size(); j++)
    			{
    				if(CompareNBTTag(lt1, list2.get(j), partial))
    				{
    					continue topLoop;
    				}
    			}
    			
    			return false; // Couldn't find requested tag in list
    		}
    	} else if(tag1 instanceof IntArrayNBT && tag2 instanceof IntArrayNBT)
    	{
    		IntArrayNBT list1 = (IntArrayNBT)tag1;
    		IntArrayNBT list2 = (IntArrayNBT)tag2;
    		
    		if(list1.getIntArray().length > list2.getIntArray().length || (!partial && list1.getIntArray().length != list2.getIntArray().length))
    		{
    			return false; // Sample is missing requested tags or is not exact
    		}
    		
    		List<Integer> usedIdxs = new ArrayList<>(); // Duplicate control
    		
    		topLoop:
    		for(int i = 0; i < list1.getIntArray().length; i++)
    		{
    			for(int j = 0; j < list2.getIntArray().length; j++)
    			{
    				if(!usedIdxs.contains(j) && list1.getIntArray()[i] == list2.getIntArray()[j])
    				{
    					usedIdxs.add(j);
    					continue topLoop;
    				}
    			}
    			
    			return false; // Couldn't find requested integer in list
    		}
    		
    		return true;
    	} else if(tag1 instanceof ByteArrayNBT && tag2 instanceof ByteArrayNBT)
    	{
    		ByteArrayNBT list1 = (ByteArrayNBT)tag1;
    		ByteArrayNBT list2 = (ByteArrayNBT)tag2;
    		
    		if(list1.getByteArray().length > list2.getByteArray().length || (!partial && list1.getByteArray().length != list2.getByteArray().length))
    		{
    			return false; // Sample is missing requested tags or is not exact for non-partial match
    		}
    		
    		List<Integer> usedIdxs = new ArrayList<>(); // Duplicate control
    		
    		topLoop:
    		for(int i = 0; i < list1.getByteArray().length; i++)
    		{
    			for(int j = 0; j < list2.getByteArray().length; j++)
    			{
    				if(!usedIdxs.contains(j) && list1.getByteArray()[i] == list2.getByteArray()[j])
    				{
    					usedIdxs.add(j);
    					continue topLoop;
    				}
    			}
    			
    			return false; // Couldn't find requested integer in list
    		}
    	} else if(tag1 instanceof LongArrayNBT && tag2 instanceof LongArrayNBT)
    	{
    		LongArrayNBT list1 = (LongArrayNBT)tag1;
    		LongArrayNBT list2 = (LongArrayNBT)tag2;
    		
    		final long[] la1 = list1.getAsLongArray();
    		final long[] la2 = list2.getAsLongArray();
    		
    		if(la1.length > la2.length || (!partial && la1.length != la2.length))
    		{
    			return false; // Sample is missing requested tags or is not exact for non-partial match
    		}
    		
    		List<Integer> usedIdxs = new ArrayList<>(); // Duplicate control
    		
    		topLoop:
            for(long l : la1)
            {
                for(int j = 0; j < la2.length; j++)
                {
                    if(!usedIdxs.contains(j) && l == la2[j])
                    {
                        usedIdxs.add(j);
                        continue topLoop;
                    }
                }
            
                return false; // Couldn't find requested integer in list
            }
        } else if(tag1 instanceof StringNBT && tag2 instanceof StringNBT)
    	{
    		return tag1.equals(tag2);
    	} else if(tag1 instanceof NumberNBT && tag2 instanceof NumberNBT) // Standardize numbers to not care about format
    	{
    		Number num1 = NBTConverter.getNumber(tag1);
    		Number num2 = NBTConverter.getNumber(tag2);
    		
    		// Check if floating point precesion needs to be preserved in comparison
    		if(tag1 instanceof FloatNBT || tag1 instanceof DoubleNBT || tag2 instanceof FloatNBT || tag2 instanceof DoubleNBT)
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
    
    private static boolean CompareNBTTagCompound(CompoundNBT reqTags, CompoundNBT sample, boolean partial)
    {
        if(isEmptyNBT(reqTags) != isEmptyNBT(sample)) // One is null, the other is not
    	{
    		return false;
    	} else if(isEmptyNBT(reqTags)) // The opposing tag will always be null at this point if the other already is
    	{
    		return true;
    	}
    	
    	for(String key : reqTags.keySet())
    	{
    		if(!sample.contains(key))
    		{
    			return false;
    		} else if(!CompareNBTTag(reqTags.get(key), sample.get(key), partial))
    		{
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    private static boolean isEmptyNBT(INBT tag)
    {
        if(tag == null) return true;
        if(tag instanceof ListNBT) return ((ListNBT)tag).isEmpty();
        if(tag instanceof CompoundNBT) return ((CompoundNBT)tag).isEmpty();
        return false;
    }
    
    @Deprecated
    public static boolean OreDictionaryMatch(String name, ItemStack stack)
    {
    	return stack != null && !StringUtils.isNullOrEmpty(name) && new TagIngredient(name).apply(stack);
    }
    
    @Deprecated
    public static boolean OreDictionaryMatch(String name, CompoundNBT tags, ItemStack stack, boolean nbtCheck, boolean partialNBT)
    {
        if(!nbtCheck) return stack != null && !StringUtils.isNullOrEmpty(name) && new TagIngredient(name).apply(stack);
        return OreDictionaryMatch(new TagIngredient(name), tags, stack, nbtCheck, partialNBT);
    }
    
    /**
     * Check if the item stack is part of the ore dictionary listing with the given ore ingredient while also comparing NBT tags
     */
    public static boolean OreDictionaryMatch(TagIngredient ore, CompoundNBT tags, ItemStack stack, boolean nbtCheck, boolean partialNBT)
    {
        if(stack == null || ore == null) return false;
        return ore.apply(stack) && (!nbtCheck || CompareNBTTagCompound(stack.getTag(), tags, partialNBT));
    }
    
    /**
     * Check if the two stacks match directly or through ore dictionary listings
     */
    @Deprecated
    public static boolean AllMatch(ItemStack stack1, ItemStack stack2)
    {
        return AllMatch(stack1, stack2, false, false);
    }
    
    /**
     * Check if the two stacks match directly or through ore dictionary listings
     */
    public static boolean AllMatch(ItemStack stack1, ItemStack stack2, boolean nbtCheck, boolean partialNBT)
    {
        if(stack1 == stack2) return true; // Both null or same instance
        if(stack1 == null) return false; // One is null the other is not
        if(nbtCheck && !CompareNBTTagCompound(stack1.getTag(), stack2.getTag(), partialNBT)) return false; // NBT check failed
    	if(StackMatch(stack1, stack2, false, false)) return true; // Stacks are compatible (NBT was already checked at this point)
    	
        // Final Ore Dictionary test...
    	Set<ResourceLocation> oreIDs1 = stack1.getItem().getTags();//.getOreIDs(stack1);
    	Set<ResourceLocation> oreIDs2 = stack2.getItem().getTags();//.getOreIDs(stack2);
    	
    	// Search all ore dictionary listings for matches
    	for(ResourceLocation i1 : oreIDs1)
    	{
    	    // Shared ore dictionary entries found
    	    if(oreIDs2.parallelStream().anyMatch(i1::equals)) return true;
    	}
    	
    	return false; // No shared ore dictionary types
    }
}
