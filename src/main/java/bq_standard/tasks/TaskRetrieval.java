package bq_standard.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.quests.tasks.advanced.IContainerTask;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.ItemComparison;
import betterquesting.utils.JsonHelper;
import bq_standard.client.gui.tasks.GuiTaskRetrieval;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TaskRetrieval extends TaskBase implements IContainerTask
{
	public ArrayList<BigItemStack> requiredItems = new ArrayList<BigItemStack>();
	public HashMap<UUID, int[]> userProgress = new HashMap<UUID, int[]>();
	boolean partialMatch = true;
	boolean ignoreNBT = false;
	public boolean consume = true;
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.retrieval";
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
		if(!player.isEntityAlive() || player.inventory == null || this.isComplete(player.getUniqueID()) || requiredItems.size() <= 0)
		{
			return;
		}
		
		boolean flag = true;
		
		int[] progress = userProgress.get(player.getUniqueID());
		progress = progress == null || progress.length != requiredItems.size()? new int[requiredItems.size()] : progress;
		
		for(int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			for(int j = 0; j < requiredItems.size(); j++)
			{
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack == null)
				{
					break;
				}
				
				BigItemStack rStack = requiredItems.get(j);
				
				if(rStack == null || progress[j] >= rStack.stackSize)
				{
					continue;
				}

				int remaining = rStack.stackSize - progress[j];
				
				if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch))
				{
					if(consume)
					{
						ItemStack removed = player.inventory.decrStackSize(i, remaining);
						progress[j] += removed.stackSize;
					} else
					{
						progress[j] += Math.min(remaining, stack.stackSize);
					}
				}
			}
		}
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			BigItemStack rStack = requiredItems.get(j);
			
			if(rStack == null || progress[j] >= rStack.stackSize)
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

	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("partialMatch", partialMatch);
		json.addProperty("ignoreNBT", ignoreNBT);
		json.addProperty("consume", consume);
		
		JsonArray itemArray = new JsonArray();
		for(BigItemStack stack : this.requiredItems)
		{
			itemArray.add(JsonHelper.ItemStackToJson(stack, new JsonObject()));
		}
		json.add("requiredItems", itemArray);
		
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
		
		partialMatch = JsonHelper.GetBoolean(json, "partialMatch", partialMatch);
		ignoreNBT = JsonHelper.GetBoolean(json, "ignoreNBT", ignoreNBT);
		consume = JsonHelper.GetBoolean(json, "consume", true);
		
		requiredItems.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "requiredItems"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			BigItemStack item = JsonHelper.JsonToItemStack(entry.getAsJsonObject());
			
			if(item != null)
			{
				requiredItems.add(item);
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
				BQ_Standard.logger.log(Level.ERROR, "Unable to load user progress for task", e);
				continue;
			}
			
			int[] data = new int[requiredItems.size()];
			JsonArray dJson = JsonHelper.GetArray(entry.getAsJsonObject(), "data");
			for(int i = 0; i < data.length && i < dJson.size(); i++)
			{
				try
				{
					data[i] = dJson.get(i).getAsInt();
				} catch(Exception e)
				{
					BQ_Standard.logger.log(Level.ERROR, "Incorrect task progress format", e);
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
		return new GuiTaskRetrieval(this, screen, posX, posY, sizeX, sizeY);
	}

	@Override
	public boolean canAcceptFluid(UUID owner, Fluid fluid)
	{
		return false;
	}

	@Override
	public boolean canAcceptItem(UUID owner, ItemStack stack)
	{
		if(owner == null || stack == null || !consume || isComplete(owner) || requiredItems.size() <= 0)
		{
			return false;
		}
		
		int[] progress = userProgress.get(owner);
		progress = progress == null || progress.length != requiredItems.size()? new int[requiredItems.size()] : progress;
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			BigItemStack rStack = requiredItems.get(j);
			
			if(rStack == null || progress[j] >= rStack.stackSize)
			{
				continue;
			}
			
			if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public FluidStack submitFluid(UUID owner, FluidStack fluid)
	{
		return fluid;
	}

	@Override
	public void submitItem(UUID owner, Slot input, Slot output)
	{
		ItemStack stack = input.getStack();
		
		if(owner == null || stack == null || !consume || isComplete(owner))
		{
			return;
		}
		
		int[] progress = userProgress.get(owner);
		progress = progress == null || progress.length != requiredItems.size()? new int[requiredItems.size()] : progress;
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			if(stack == null)
			{
				break;
			}
			
			BigItemStack rStack = requiredItems.get(j);
			
			if(rStack == null || progress[j] >= rStack.stackSize)
			{
				continue;
			}

			int remaining = rStack.stackSize - progress[j];
			
			if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch))
			{
				int removed = Math.min(stack.stackSize, remaining);
				stack.stackSize -= removed;
				progress[j] += removed;
				
				if(stack.stackSize <= 0)
				{
					break;
				}
			}
		}
		
		userProgress.put(owner, progress);
		
		boolean flag = true;
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			BigItemStack rStack = requiredItems.get(j);
			
			if(rStack == null || progress[j] >= rStack.stackSize)
			{
				continue;
			}
			
			flag = false;
			break;
		}
		
		if(flag)
		{
			completeUsers.add(owner);
		}
		
		if(stack == null || stack.stackSize <= 0)
		{
			input.putStack(null);
		} else
		{
			input.putStack(stack);
		}
	}
}
