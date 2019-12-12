package betterquesting.api.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;

public class TagIngredient
{
    private final String tagName;
    private final Collection<Item> items = new HashSet<>();
    private final Collection<Block> blocks = new HashSet<>();
    private int lastItemCheck = -1;
    private int lastBlockCheck = -1;
    
    public TagIngredient(String tagName)
    {
        this.tagName = tagName;
    }
    
    public Collection<Item> getMatchingItems()
    {
        return items;
    }
    
    public Collection<Block> getMatchingBlocks()
    {
        return blocks;
    }
    
    private void refresh()
    {
        if(StringUtils.isNullOrEmpty(tagName)) return;
        
        if(lastItemCheck < 0 || lastItemCheck != ItemTags.getGeneration())
        {
            items.clear();
            Tag<Item> iTag = ItemTags.getCollection().get(new ResourceLocation(tagName));
            Collection<Item> tmpI = iTag == null ? null : iTag.getAllElements();
            if(tmpI != null) items.addAll(tmpI);
            lastItemCheck = ItemTags.getGeneration();
        }
        
        if(lastBlockCheck < 0 || lastBlockCheck != BlockTags.getGeneration())
        {
            blocks.clear();
            Tag<Block> bTag = BlockTags.getCollection().get(new ResourceLocation(tagName));
            Collection<Block> tmpB = bTag == null ? null : bTag.getAllElements();
            if(tmpB != null) blocks.addAll(tmpB);
            lastBlockCheck = BlockTags.getGeneration();
        }
    }
    
    public boolean apply(BlockState state)
    {
        return state == null || apply(state.getBlock());
    }
    
    public boolean apply(Block block)
    {
        if(block == null || block == Blocks.AIR) return true;
        refresh();
        return blocks.parallelStream().anyMatch((val) -> val.equals(block));
    }
    
    public boolean apply(ItemStack stack)
    {
        return stack == null || apply(stack.getItem());
    }
    
    public boolean apply(Item item)
    {
        if(item == null || item == Items.AIR) return true;
        refresh();
        return items.parallelStream().anyMatch((val) -> val.equals(item));
    }
}
