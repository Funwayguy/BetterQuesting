package betterquesting.utils;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Purpose built container class for holding ItemStacks larger than 127. <br>
 * <b>For storage purposes only!
 */
public class BigItemStack
{
	public int stackSize = 0;
	public String oreDict = "";
	ItemStack baseStack = new ItemStack(Blocks.stone); // Ensures that this base stack is never null
	
	public BigItemStack(ItemStack stack)
	{
		baseStack = stack.copy();
		this.stackSize = baseStack.stackSize;
		baseStack.stackSize = 1;
	}
	
	public BigItemStack(Block block)
	{
		this(block, 1);
	}
	
	public BigItemStack(Block block, int amount)
	{
		this(block, amount, 0);
	}
	
	public BigItemStack(Block block, int amount, int damage)
	{
		this(Item.getItemFromBlock(block), amount, damage);
	}
	
	public BigItemStack(Item item)
	{
		this(item, 1);
	}
	
	public BigItemStack(Item item, int amount)
	{
		this(item, amount, 0);
	}
	
	public BigItemStack(Item item, int amount, int damage)
	{
		baseStack = new ItemStack(item, 1, damage);
		this.stackSize = amount;
	}
	
	/**
	 * @return ItemStack this BigItemStack is based on. Changing the base stack size does NOT affect the BigItemStack's size
	 */
	public ItemStack getBaseStack()
	{
		return baseStack;
	}
	
	/**
	 * Shortcut method to the NBTTagCompound in the base ItemStack
	 */
	public NBTTagCompound GetTagCompound()
	{
		return baseStack.getTagCompound();
	}
	
	/**
	 * Shortcut method to the NBTTagCompound in the base ItemStack
	 */
	public void SetTagCompound(NBTTagCompound tags)
	{
		baseStack.setTagCompound(tags);
	}
	
	/**
	 * Shortcut method to the NBTTagCompound in the base ItemStack
	 */
	public boolean HasTagCompound()
	{
		return baseStack.hasTagCompound();
	}
	
	/**
	 * Breaks down this big stack into smaller ItemStacks for Minecraft to use (Individual stack size is dependent on the item)
	 */
	public ArrayList<ItemStack> getCombinedStacks()
	{
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		int tmp1 = Math.max(1, stackSize); // Guarantees this method will return at least 1 item
		
		while(tmp1 > 0)
		{
			int size = Math.min(tmp1, baseStack.getMaxStackSize());
			ItemStack stack = baseStack.copy();
			stack.stackSize = size;
			list.add(stack);
			tmp1 -= size;
		}
		
		return list;
	}
	
	public BigItemStack copy()
	{
		BigItemStack stack = new BigItemStack(baseStack.copy());
		stack.stackSize = this.stackSize;
		return stack;
	}
	
	@Override
	public boolean equals(Object stack)
	{
		if(stack instanceof ItemStack)
		{
			return baseStack.equals(stack);
		} else
		{
			return super.equals(stack);
		}
	}
	
	public static BigItemStack loadItemStackFromNBT(NBTTagCompound tags)
	{
		int count = tags.getInteger("Count");
		String dict = tags.getString("OreDict");
		ItemStack miniStack = ItemStack.loadItemStackFromNBT(tags);
		BigItemStack bigStack = new BigItemStack(miniStack);
		bigStack.stackSize = count;
		bigStack.oreDict = dict;
		return bigStack;
	}
	
	public void readFromNBT(NBTTagCompound tags)
	{
		stackSize = tags.getInteger("Count");
		oreDict = tags.getString("OreDict");
		baseStack.readFromNBT(tags);
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound tags)
	{
		baseStack.writeToNBT(tags);
		tags.setInteger("Count", stackSize);
		tags.setString("OreDict", oreDict);
		return tags;
	}
}
