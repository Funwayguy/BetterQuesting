package betterquesting.api.utils;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * This utility either loads the original object or converts invalid objects to place holders
 */
public interface IMakePlaceholder
{
	public Entity convertPlaceholder(Entity entity, World world, NBTTagCompound nbt);
	public BigItemStack convertPlaceholder(Item item, String name, int count, int damage, String oreDict, NBTTagCompound nbt);
	public FluidStack convertPlaceholder(Fluid fluid, String name, int amount, NBTTagCompound nbt);
}
