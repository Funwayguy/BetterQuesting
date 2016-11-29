package betterquesting.api.placeholders;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import betterquesting.api.utils.BigItemStack;

/**
 * In charge of safely converting to or from placeholder objects
 */
public class PlaceholderConverter
{
	public static Entity convertEntity(Entity orig, World world, NBTTagCompound nbt)
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
	
	public static BigItemStack convertItem(Item item, String name, int count, int damage, String oreDict, NBTTagCompound nbt)
	{
		if(item == null)
		{
			BigItemStack stack = new BigItemStack(ItemPlaceholder.placeholder, count, damage);
			stack.oreDict = oreDict;
			stack.SetTagCompound(new NBTTagCompound());
			stack.GetTagCompound().setString("orig_id", name);
			if(nbt != null)
			{
				stack.GetTagCompound().setTag("orig_tag", nbt);
			}
			return stack;
		} else if(item == ItemPlaceholder.placeholder)
		{
			if(nbt != null)
			{
				Item restored = (Item)Item.REGISTRY.getObject(new ResourceLocation(nbt.getString("orig_id")));
				
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
	
	public static FluidStack convertFluid(Fluid fluid, String name, int amount, NBTTagCompound nbt)
	{
		if(fluid == null)
		{
			FluidStack stack = new FluidStack(FluidPlaceholder.fluidPlaceholder, amount);
			NBTTagCompound orig = new NBTTagCompound();
			orig.setString("orig_id", name);
			if(nbt != null)
			{
				orig.setTag("orig_tag", nbt);
			}
			stack.tag = orig;
			return stack;
		} else if(fluid == FluidPlaceholder.fluidPlaceholder && nbt != null)
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
