package betterquesting.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import betterquesting.EntityPlaceholder;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.IMakePlaceholder;
import betterquesting.core.BetterQuesting;

public class MakePlaceholder implements IMakePlaceholder
{
	public static final MakePlaceholder INSTANCE = new MakePlaceholder();
	
	private MakePlaceholder()
	{
	}
	
	@Override
	public Entity convertPlaceholder(Entity orig, World world, NBTTagCompound nbt)
	{
		Entity entity = orig;
		
		if(orig == null)
		{
			entity = new EntityPlaceholder(world);
			((EntityPlaceholder)entity).SetOriginalTags(nbt);
		} else if(orig instanceof EntityPlaceholder)
		{
			EntityPlaceholder p = (EntityPlaceholder)orig;
			Entity tmp = EntityList.createEntityFromNBT(p.GetOriginalTags(), world);
			entity = tmp != null? tmp : p;
		}
		
		return entity;
	}
	
	@Override
	public BigItemStack convertPlaceholder(Item item, String name, int count, int damage, String oreDict, NBTTagCompound nbt)
	{
		if(item == null)
		{
			BigItemStack stack = new BigItemStack(BetterQuesting.placeholder, count, damage);
			stack.oreDict = oreDict;
			stack.SetTagCompound(new NBTTagCompound());
			stack.GetTagCompound().setString("orig_id", name);
			if(nbt != null)
			{
				stack.GetTagCompound().setTag("orig_tag", nbt);
			}
			return stack;
		} else if(item == BetterQuesting.placeholder)
		{
			if(nbt != null)
			{
				Item restored = (Item)Item.itemRegistry.getObject(nbt.getString("orig_id"));
				
				if(restored != null)
				{
					BigItemStack stack = new BigItemStack(restored, count, damage);
					stack.oreDict = oreDict;
					
					if(nbt.hasKey("orig_tag"))
					{
						stack.SetTagCompound(nbt.getCompoundTag("orig_tag"));
					}
					
					return stack;
				}
			}
		}
		
		BigItemStack stack = new BigItemStack(item, count, damage);
		stack.oreDict = oreDict;
		
		if(nbt != null)
		{
			stack.SetTagCompound(nbt);
		}
		
		return stack;
	}
	
	@Override
	public FluidStack convertPlaceholder(Fluid fluid, String name, int amount, NBTTagCompound nbt)
	{
		if(fluid == null)
		{
			FluidStack stack = new FluidStack(BetterQuesting.fluidPlaceholder, amount);
			NBTTagCompound orig = new NBTTagCompound();
			orig.setString("orig_id", name);
			if(nbt != null)
			{
				orig.setTag("orig_tag", nbt);
			}
			stack.tag = orig;
			return stack;
		} else if(fluid == BetterQuesting.fluidPlaceholder && nbt != null)
		{
			Fluid restored = FluidRegistry.getFluid(nbt.getString("orig_id"));
			
			if(restored != null)
			{
				FluidStack stack = new FluidStack(restored, amount);
				
				if(nbt.hasKey("orig_tag"))
				{
					stack.tag = nbt.getCompoundTag("orig_tag");
				}
				
				return stack;
			}
		}
		
		FluidStack stack = new FluidStack(fluid, amount);
		
		if(nbt != null)
		{
			stack.tag = nbt;
		}
		
		return stack;
	}
	
}
