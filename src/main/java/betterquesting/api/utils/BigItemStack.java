package betterquesting.api.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StringUtils;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Purpose built container class for holding ItemStacks larger than 127. <br>
 * <b>For storage purposes only!
 */
public class BigItemStack {
    private static final OreIngredient NO_ORE = new OreIngredient("");
    private final ItemStack baseStack;
    public int stackSize;
    private String oreDict = "";
    private OreIngredient oreIng = NO_ORE;

    public BigItemStack(ItemStack stack) {
        baseStack = stack.copy();
        this.stackSize = baseStack.getCount();
        baseStack.setCount(1);
    }

    public BigItemStack(@Nonnull Block block) {
        this(block, 1);
    }

    public BigItemStack(@Nonnull Block block, int amount) {
        this(block, amount, 0);
    }

    public BigItemStack(@Nonnull Block block, int amount, int damage) {
        this(Item.getItemFromBlock(block), amount, damage);
    }

    public BigItemStack(@Nonnull Item item) {
        this(item, 1);
    }

    public BigItemStack(@Nonnull Item item, int amount) {
        this(item, amount, 0);
    }

    public BigItemStack(@Nonnull Item item, int amount, int damage) {
        baseStack = new ItemStack(item, 1, damage);
        this.stackSize = amount;
    }

    /**
     * @return ItemStack this BigItemStack is based on. Changing the base stack size does NOT affect the BigItemStack's size
     */
    public ItemStack getBaseStack() {
        return baseStack;
    }

    public boolean hasOreDict() {
        return !StringUtils.isNullOrEmpty(this.oreDict) && this.oreIng.getMatchingStacks().length > 0;
    }

    @Nonnull
    public String getOreDict() {
        return this.oreDict;
    }

    @Nonnull
    public OreIngredient getOreIngredient() {
        return this.oreIng;
    }

    public BigItemStack setOreDict(@Nonnull String ore) {
        this.oreDict = ore;
        this.oreIng = ore.length() <= 0 ? NO_ORE : new OreIngredient(ore);
        return this;
    }

    /**
     * Shortcut method to the NBTTagCompound in the base ItemStack
     */
    public NBTTagCompound GetTagCompound() {
        return baseStack.getTagCompound();
    }

    /**
     * Shortcut method to the NBTTagCompound in the base ItemStack
     */
    public void SetTagCompound(NBTTagCompound tags) {
        baseStack.setTagCompound(tags);
    }

    /**
     * Shortcut method to the NBTTagCompound in the base ItemStack
     */
    public boolean HasTagCompound() {
        return baseStack.hasTagCompound();
    }

    /**
     * Breaks down this big stack into smaller ItemStacks for Minecraft to use (Individual stack size is dependent on the item)
     */
    public List<ItemStack> getCombinedStacks() {
        List<ItemStack> list = new ArrayList<>();
        int tmp1 = Math.max(1, stackSize); // Guarantees this method will return at least 1 item

        while (tmp1 > 0) {
            int size = Math.min(tmp1, baseStack.getMaxStackSize());
            ItemStack stack = baseStack.copy();
            stack.setCount(size);
            list.add(stack);
            tmp1 -= size;
        }

        return list;
    }

    public BigItemStack copy() {
        BigItemStack stack = new BigItemStack(baseStack.copy());
        stack.stackSize = this.stackSize;
        stack.oreDict = this.oreDict;
        stack.oreIng = this.oreIng;
        return stack;
    }

    @Override
    public boolean equals(Object stack) {
        if (stack instanceof ItemStack) {
            return baseStack.isItemEqual((ItemStack) stack) && ItemStack.areItemStackTagsEqual(baseStack, (ItemStack) stack);
        } else {
            return super.equals(stack);
        }
    }

    public BigItemStack(@Nonnull NBTTagCompound tags) // Can load normal ItemStack NBTs. Does NOT deal with placeholders
    {
        NBTTagCompound itemNBT = tags.copy();
        itemNBT.setInteger("Count", 1);
        if (tags.hasKey("id", 99)) {
            itemNBT.setString("id", "" + tags.getShort("id"));
        }
        this.stackSize = tags.getInteger("Count");
        this.setOreDict(tags.getString("OreDict"));
        this.baseStack = new ItemStack(itemNBT); // Minecraft does the ID conversions for me
        if (tags.getShort("Damage") < 0) this.baseStack.setItemDamage(OreDictionary.WILDCARD_VALUE);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tags) {
        baseStack.writeToNBT(tags);
        tags.setInteger("Count", stackSize);
        tags.setString("OreDict", oreDict);
        return tags;
    }
}
