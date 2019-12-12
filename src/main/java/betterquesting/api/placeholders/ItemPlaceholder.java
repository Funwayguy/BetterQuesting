package betterquesting.api.placeholders;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPlaceholder extends Item
{
	public static Item placeholder = new ItemPlaceholder();
	
	// Used solely for retaining info to missing items
	public ItemPlaceholder()
	{
	    super(new Item.Properties());
	}
	
    /**
     * allows items to add custom lines of information to the mouseover description
     */
	@Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		if(!stack.hasTag())
		{
			tooltip.add(new StringTextComponent("ERROR: Original information missing!"));
			return;
		}
		
		tooltip.add(new StringTextComponent("Original ID: " + stack.getTag().getString("orig_id")));
	}

    /**
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
	@Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean held)
    {
    	if(!stack.hasTag() || !(entity instanceof PlayerEntity) || world.getGameTime()%100 != 0) // Process this only once a second
    	{
    		return;
    	}
    	
    	PlayerEntity player = (PlayerEntity)entity;
    	
    	CompoundNBT tags = stack.getTag();
    	Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tags.getString("orig_id")));
    	CompoundNBT t = tags.contains("orig_tag")? tags.getCompound("orig_tag") : null;
    	
    	if(i != null)
    	{
    		ItemStack converted = new ItemStack(i, stack.getCount());
    		converted.setTag(t);
    		player.inventory.setInventorySlotContents(slot, converted);
    	}
    }
}
