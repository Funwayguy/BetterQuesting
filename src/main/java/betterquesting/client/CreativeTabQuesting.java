package betterquesting.client;

import betterquesting.core.BetterQuesting;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import javax.annotation.Nonnull;

public class CreativeTabQuesting extends CreativeTabs
{
	public CreativeTabQuesting()
	{
		super(BetterQuesting.MODID);
	}
 
	@Nonnull
    @Override
    public Item getTabIconItem()
    {
        return BetterQuesting.extraLife;
    }
}
