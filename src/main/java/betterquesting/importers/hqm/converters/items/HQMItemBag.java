package betterquesting.importers.hqm.converters.items;

import betterquesting.api.utils.BigItemStack;
import betterquesting.core.BetterQuesting;
import net.minecraft.nbt.NBTTagCompound;

public class HQMItemBag implements HQMItem
{
	@Override
	public BigItemStack convertItem(int damage, int amount, NBTTagCompound tags)
	{
		return new BigItemStack(BetterQuesting.lootChest, amount, damage * 25);
	}
}
