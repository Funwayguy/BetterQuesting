package betterquesting.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.core.BetterQuesting;

public class ItemGuideBook extends Item
{
	public ItemGuideBook()
	{
		//this.setTextureName("book_written");
		this.setUnlocalizedName("betterquesting.guide");
		this.setCreativeTab(BetterQuesting.tabQuesting);
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
