package betterquesting.client;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import betterquesting.core.BetterQuesting;

public class CreativeTabQuesting extends CreativeTabs
{
	private final ItemStack tabStack;
	
	public CreativeTabQuesting()
	{
		super(BetterQuesting.MODID);
		this.tabStack = new ItemStack(BetterQuesting.extraLife);
	}

	@Override
	public ItemStack getTabIconItem()
	{
		return tabStack;
	}
}
