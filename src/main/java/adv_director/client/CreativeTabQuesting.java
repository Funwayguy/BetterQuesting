package adv_director.client;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import adv_director.core.AdvDirector;

public class CreativeTabQuesting extends CreativeTabs
{
	public CreativeTabQuesting()
	{
		super(AdvDirector.MODID);
	}

	@Override
	public Item getTabIconItem()
	{
		return AdvDirector.extraLife;
	}
}
