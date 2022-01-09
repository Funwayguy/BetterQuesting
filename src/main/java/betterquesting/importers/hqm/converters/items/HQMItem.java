package betterquesting.importers.hqm.converters.items;

import betterquesting.api.utils.BigItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface HQMItem
{
	BigItemStack convertItem(int damage, int amount, NBTTagCompound tags);
}
