package betterquesting.api.placeholders;

import betterquesting.api.utils.BigItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

/**
 * In charge of safely converting to or from placeholder objects
 */
public class PlaceholderConverter
{
	public static Entity convertEntity(Entity orig, World world, CompoundNBT nbt)
	{
		Entity entity = orig;
		
		if(orig == null)
		{
			entity = new EntityPlaceholder(world);
			((EntityPlaceholder)entity).SetOriginalTags(nbt);
		} else if(orig instanceof EntityPlaceholder)
		{
			EntityPlaceholder p = (EntityPlaceholder)orig;
			Optional<Entity> tmp = EntityType.loadEntityUnchecked(p.GetOriginalTags(), world);
			entity = tmp.orElse(p);
		}
		
		return entity;
	}
	
	public static BigItemStack convertItem(Item item, String name, int count, String oreDict, CompoundNBT nbt)
	{
		if(item == null)
		{
			BigItemStack stack = new BigItemStack(ItemPlaceholder.placeholder, count).setOreDict(oreDict);
			stack.SetTagCompound(new CompoundNBT());
			stack.GetTagCompound().putString("orig_id", name);
			if(nbt != null) stack.GetTagCompound().put("orig_tag", nbt);
			return stack;
		} else if(item == ItemPlaceholder.placeholder)
		{
			if(nbt != null)
			{
				Item restored = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("orig_id")));
				
				if(restored != null && restored != Items.AIR)
				{
					BigItemStack stack = new BigItemStack(restored, count).setOreDict(oreDict);
					if(nbt.contains("orig_tag")) stack.SetTagCompound(nbt.getCompound("orig_tag"));
					
					return stack;
				}
			}
		}
		
		BigItemStack stack = new BigItemStack(item, count).setOreDict(oreDict);
		if(nbt != null) stack.SetTagCompound(nbt);
		
		return stack;
	}
	
	public static FluidStack convertFluid(Fluid fluid, String name, int amount, CompoundNBT nbt)
	{
		if(fluid == null)
		{
			FluidStack stack = new FluidStack(FluidPlaceholder.fluidPlaceholder, amount);
			CompoundNBT orig = new CompoundNBT();
			orig.putString("orig_id", name);
			if(nbt != null) orig.put("orig_tag", nbt);
			stack.setTag(orig);
			return stack;
		} else if(fluid == FluidPlaceholder.fluidPlaceholder && nbt != null)
		{
			Fluid restored = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(nbt.getString("orig_id")));
			
			if(restored != null)
			{
				FluidStack stack = new FluidStack(restored, amount);
				if(nbt.contains("orig_tag")) stack.setTag(nbt.getCompound("orig_tag"));
				return stack;
			}
		}
		
		FluidStack stack = new FluidStack(fluid, amount);
		if(nbt != null) stack.setTag(nbt);
		
		return stack;
	}
}
