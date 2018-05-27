package betterquesting.client;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import betterquesting.core.BetterQuesting;

public class CreativeTabQuesting extends CreativeTabs
{
	private ItemStack tabStack;
	
	public CreativeTabQuesting()
	{
		super(BetterQuesting.MODID);
	}
	
	@Override
	public ItemStack getTabIconItem()
	{
		if(tabStack == null)
		{
			this.tabStack = new ItemStack(BetterQuesting.extraLife);
		}
		
		return tabStack;
	}
}
