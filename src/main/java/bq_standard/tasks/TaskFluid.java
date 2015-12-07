package bq_standard.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.core.BetterQuesting;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import bq_standard.client.gui.tasks.GuiTaskFluid;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TaskFluid extends TaskBase
{
	public ArrayList<FluidStack> requiredFluids = new ArrayList<FluidStack>();
	public HashMap<UUID, int[]> userProgress = new HashMap<UUID, int[]>();
	public boolean consume = true;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.task.fluid";
	}
	
	@Override
	public void Update(EntityPlayer player)
	{
		if(!consume && player.ticksExisted%200 == 0) // Every ~10 seconds auto detect this quest as long as it isn't consuming items
		{
			Detect(player);
		}
	}

	@Override
	public void Detect(EntityPlayer player)
	{
		if(!player.isEntityAlive() || player.inventory == null || this.isComplete(player) || requiredFluids.size() <= 0)
		{
			return;
		}
		
		boolean flag = true;
		
		int[] progress = userProgress.get(player.getUniqueID());
		progress = progress == null || progress.length != requiredFluids.size()? new int[requiredFluids.size()] : progress;
		
		for(int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			for(int j = 0; j < requiredFluids.size(); j++)
			{
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack == null || FluidContainerRegistry.isEmptyContainer(stack))
				{
					break;
				}
				
				FluidStack rStack = requiredFluids.get(j);
				
				if(rStack == null || progress[j] >= rStack.amount)
				{
					continue;
				}
				
				int remaining = rStack.amount - progress[j];
				
				if(rStack.isFluidEqual(stack))
				{
					if(consume)
					{
						FluidStack fluid = this.getFluid(player, i, true, remaining);
						progress[j] += Math.min(remaining, fluid == null? 0 : fluid.amount);
					} else
					{
						FluidStack fluid = this.getFluid(player, i, false, remaining);
						progress[j] += Math.min(remaining, fluid == null? 0 : fluid.amount);
					}
				}
			}
		}
		
		for(int j = 0; j < requiredFluids.size(); j++)
		{
			FluidStack rStack = requiredFluids.get(j);
			
			if(rStack == null || progress[j] >= rStack.amount)
			{
				continue;
			}
			
			flag = false;
			break;
		}
		
		if(consume)
		{
			userProgress.put(player.getUniqueID(), progress);
		}
		
		if(flag)
		{
			this.completeUsers.add(player.getUniqueID());
		}
	}
	
	/**
	 * Returns the fluid drained (or can be drained) up to the specified amount
	 */
	public FluidStack getFluid(EntityPlayer player, int slot, boolean drain, int amount)
	{
		ItemStack stack = player.inventory.getStackInSlot(slot);
		
		if(stack == null || amount <= 0)
		{
			return null;
		}
		
		if(stack.getItem() instanceof IFluidContainerItem)
		{
			IFluidContainerItem container = (IFluidContainerItem)stack.getItem();
			
			return container.drain(stack, amount, drain);
			
		} else
		{
			FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
			int tmp1 = fluid.amount;
			int tmp2 = 1;
			while(fluid.amount < amount && tmp2 < stack.stackSize)
			{
				tmp2++;
				fluid.amount += tmp1;
			}
			
			if(drain)
			{
				for(; tmp2 > 0; tmp2--)
				{
					ItemStack empty = FluidContainerRegistry.drainFluidContainer(stack);
					player.inventory.decrStackSize(slot, 1);
					
					if(!player.inventory.addItemStackToInventory(empty))
					{
						player.dropPlayerItemWithRandomChoice(empty, false);
					}
				}
			}
			
			return fluid;
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("consume", consume);
		
		JsonArray itemArray = new JsonArray();
		for(FluidStack stack : this.requiredFluids)
		{
			itemArray.add(NBTConverter.NBTtoJSON_Compound(stack.writeToNBT(new NBTTagCompound()), new JsonObject()));
		}
		json.add("requiredFluids", itemArray);
		
		JsonArray progArray = new JsonArray();
		for(Entry<UUID,int[]> entry : userProgress.entrySet())
		{
			JsonObject pJson = new JsonObject();
			pJson.addProperty("uuid", entry.getKey().toString());
			JsonArray pArray = new JsonArray();
			for(int i : entry.getValue())
			{
				pArray.add(new JsonPrimitive(i));
			}
			pJson.add("data", pArray);
			progArray.add(pJson);
		}
		json.add("userProgress", progArray);
	}

	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		consume = JsonHelper.GetBoolean(json, "consume", true);
		
		requiredFluids.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "requiredFluids"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			FluidStack fluid = FluidStack.loadFluidStackFromNBT(NBTConverter.JSONtoNBT_Object(entry.getAsJsonObject(), new NBTTagCompound()));
			
			if(fluid != null)
			{
				requiredFluids.add(fluid);
			} else
			{
				continue;
			}
		}
		
		userProgress.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "userProgress"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			UUID uuid;
			try
			{
				uuid = UUID.fromString(JsonHelper.GetString(entry.getAsJsonObject(), "uuid", ""));
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to load user progress for task", e);
				continue;
			}
			
			int[] data = new int[requiredFluids.size()];
			JsonArray dJson = JsonHelper.GetArray(entry.getAsJsonObject(), "data");
			for(int i = 0; i < data.length && i < dJson.size(); i++)
			{
				try
				{
					data[i] = dJson.get(i).getAsInt();
				} catch(Exception e)
				{
					BetterQuesting.logger.log(Level.ERROR, "Incorrect task progress format", e);
				}
			}
			
			userProgress.put(uuid, data);
		}
	}

	@Override
	public void ResetProgress(UUID uuid)
	{
		completeUsers.remove(uuid);
		userProgress.remove(uuid);
	}

	@Override
	public void ResetAllProgress()
	{
		completeUsers.clear();
		userProgress.clear();
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskFluid(this, screen, posX, posY, sizeX, sizeY);
	}
}
