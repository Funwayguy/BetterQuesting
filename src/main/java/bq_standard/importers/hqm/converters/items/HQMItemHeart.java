package bq_standard.importers.hqm.converters.items;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.BigItemStack;

public class HQMItemHeart extends HQMItem
{
	@Override
	public BigItemStack convertItem(int damage, int amount, NBTTagCompound tags)
	{
		int amt = amount;
		int dmg = 0;
		
		switch(damage)
		{
			case 0:
				dmg = 2;
				break;
			case 1:
				dmg = 1;
				break;
			case 2:
				dmg = 2;
				amt *= 3;
				break;
			default:
				dmg = 0;
				break;
		}
		
		return new BigItemStack(BetterQuesting.extraLife, amt, dmg);
	}
}
