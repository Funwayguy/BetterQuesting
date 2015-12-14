package bq_standard.importers.hqm.converters.items;

import betterquesting.utils.BigItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class HQMItem
{
	public abstract BigItemStack convertItem(int damage, int amount, NBTTagCompound tags);
}
