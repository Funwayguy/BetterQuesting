package betterquesting.items;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import betterquesting.core.BetterQuesting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPlaceholder extends Item
{
	// Used solely for retaining info to missing items
	public ItemPlaceholder()
	{
		this.setTextureName("betterquesting:placeholder");
		this.setUnlocalizedName("betterquesting.placeholder");
		this.setCreativeTab(BetterQuesting.tabQuesting);
	}
	
    /**
     * allows items to add custom lines of information to the mouseover description
     */
	@Override
    @SideOnly(Side.CLIENT)
	@SuppressWarnings({"unchecked", "rawtypes"})
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		if(!stack.hasTagCompound())
		{
			list.add("ERROR: Original information missing!");
			return;
		}
		
		list.add("Original ID: " + stack.getTagCompound().getString("orig_id"));
	}
}
