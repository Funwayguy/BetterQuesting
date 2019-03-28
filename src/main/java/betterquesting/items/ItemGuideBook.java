package betterquesting.items;

import betterquesting.core.BetterQuesting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemGuideBook extends Item
{
	public ItemGuideBook()
	{
		this.setTextureName("book_written");
		this.setUnlocalizedName("betterquesting.guide");
		this.setCreativeTab(BetterQuesting.tabQuesting);
	}

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        return true;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
    	if(world.isRemote)
    	{
    		player.openGui(BetterQuesting.instance, 1, world, 0, 0, 0);
    	}
    	
        return stack;
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack)
    {
		return true;
    }
}
