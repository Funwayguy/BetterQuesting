package betterquesting.client;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import betterquesting.core.BetterQuesting;

public class CreativeTabQuesting extends CreativeTabs
{
	public CreativeTabQuesting()
	{
		super(BetterQuesting.MODID);
	}

	@Override
	public Item getTabIconItem()
	{
		return BetterQuesting.extraLife;
	}
}
