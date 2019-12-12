package betterquesting.api.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Purpose built container class for holding ItemStacks larger than 127. <br>
 * <b>For storage purposes only!
 */
public class BigItemStack
{
    private static final TagIngredient NO_ORE = new TagIngredient("");
	private final ItemStack baseStack;
	public int stackSize;
	private String oreDict = "";
	private TagIngredient oreIng = NO_ORE;
	
	public BigItemStack(ItemStack stack)
	{
		baseStack = stack.copy();
		this.stackSize = baseStack.getCount();
		baseStack.setCount(1);
	}
	
	public BigItemStack(@Nonnull Block block)
	{
		this(block, 1);
	}
	
	public BigItemStack(@Nonnull Block block, int amount)
	{
	    this.baseStack = new ItemStack(block);
	    this.stackSize = amount;
	}
	
	public BigItemStack(@Nonnull Item item)
	{
		this(item, 1);
	}
	
	public BigItemStack(@Nonnull Item item, int amount)
	{
		this.baseStack = new ItemStack(item, 1);
		this.stackSize = amount;
	}
	
	/**
	 * @return ItemStack this BigItemStack is based on. Changing the base stack size does NOT affect the BigItemStack's size
	 */
	public ItemStack getBaseStack()
	{
		return baseStack;
	}
	
	public boolean hasOreDict()
    {
        return !StringUtils.isNullOrEmpty(this.oreDict) && this.oreIng.getMatchingItems().size() > 0;
    }
	
	@Nonnull
	public String getOreDict()
    {
        return this.oreDict;
    }
    
    @Nonnull
    public TagIngredient getOreIngredient()
    {
        return this.oreIng;
    }
    
    public BigItemStack setOreDict(@Nonnull String ore)
    {
        this.oreDict = ore;
        this.oreIng = ore.length() <= 0 ? NO_ORE : new TagIngredient(ore);
        return this;
    }
	
	/**
	 * Shortcut method to the NBTTagCompound in the base ItemStack
	 */
	public CompoundNBT GetTagCompound()
	{
		return baseStack.getTag();
	}
	
	/**
	 * Shortcut method to the NBTTagCompound in the base ItemStack
	 */
	public void SetTagCompound(CompoundNBT tags)
	{
		baseStack.setTag(tags);
	}
	
	/**
	 * Shortcut method to the NBTTagCompound in the base ItemStack
	 */
	public boolean HasTagCompound()
	{
		return baseStack.hasTag();
	}
	
	/**
	 * Breaks down this big stack into smaller ItemStacks for Minecraft to use (Individual stack size is dependent on the item)
	 */
	public List<ItemStack> getCombinedStacks()
	{
		List<ItemStack> list = new ArrayList<>();
		int tmp1 = Math.max(1, stackSize); // Guarantees this method will return at least 1 item
		
		while(tmp1 > 0)
		{
			int size = Math.min(tmp1, baseStack.getMaxStackSize());
			ItemStack stack = baseStack.copy();
			stack.setCount(size);
			list.add(stack);
			tmp1 -= size;
		}
		
		return list;
	}
	
	public BigItemStack copy()
	{
		BigItemStack stack = new BigItemStack(baseStack.copy());
		stack.stackSize = this.stackSize;
		stack.oreDict = this.oreDict;
		stack.oreIng = this.oreIng;
		return stack;
	}
	
	@Override
	public boolean equals(Object stack)
	{
		if(stack instanceof ItemStack)
		{
			return baseStack.isItemEqual((ItemStack)stack) && ItemStack.areItemStackTagsEqual(baseStack, (ItemStack)stack);
		} else
		{
			return super.equals(stack);
		}
	}
	
	public BigItemStack(@Nonnull CompoundNBT tags) // Can load normal ItemStack NBTs. Does NOT deal with placeholders
	{
		CompoundNBT itemNBT = tags.copy();
		itemNBT.putInt("Count", 1);
		if(tags.contains("id", 99))
        {
            itemNBT.putString("id", "" + tags.getShort("id"));
        }
		this.stackSize = tags.getInt("Count");
		this.setOreDict(tags.getString("OreDict"));
        this.baseStack = ItemStack.read(itemNBT); // Minecraft does the ID conversions for me
	}
	
	public CompoundNBT writeToNBT(CompoundNBT tags)
	{
		baseStack.write(tags);
		tags.putInt("Count", stackSize);
		tags.putString("OreDict", oreDict);
		return tags;
	}
}
