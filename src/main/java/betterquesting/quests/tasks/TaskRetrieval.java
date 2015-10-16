package betterquesting.quests.tasks;

import java.awt.Color;
import java.util.ArrayList;
import org.apache.logging.log4j.Level;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.client.GuiQuesting;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.ItemComparison;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.realmsclient.gui.ChatFormatting;

public class TaskRetrieval extends TaskBase
{
	public ArrayList<ItemStack> requiredItems = new ArrayList<ItemStack>();
	boolean partialMatch = true;
	boolean ignoreNBT = false;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.quest.retrieval.name";
	}

	@Override
	public void Update(EntityPlayer player)
	{
	}

	@Override
	public void Detect(EntityPlayer player)
	{
		if(!player.isEntityAlive() || player.inventory == null || this.isComplete(player))
		{
			return;
		}
		
		boolean flag = true;
		
		topLoop:
		for(ItemStack reqStack : requiredItems)
		{
			int count = 0;
			
			for(int i = 0; i < player.inventory.getSizeInventory(); i++)
			{
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack != null)
				{
					if(ItemComparison.StackMatch(reqStack, stack, !ignoreNBT, partialMatch))
					{
						count += stack.stackSize;
						
						if(count >= reqStack.stackSize)
						{
							continue topLoop;
						}
					}
				}
			}
			
			flag = false;
			break;
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
		
		json.add("partialMatch", new JsonPrimitive(this.partialMatch));
		json.add("ignoreNBT", new JsonPrimitive(this.ignoreNBT));
		
		JsonArray itemArray = new JsonArray();
		for(ItemStack stack : this.requiredItems)
		{
			itemArray.add(NBTConverter.NBTtoJSON_Compound(stack.writeToNBT(new NBTTagCompound()), new JsonObject()));
		}
		json.add("requiredItems", itemArray);
	}

	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		partialMatch = JsonHelper.GetBoolean(json, "partialMatch", partialMatch);
		ignoreNBT = JsonHelper.GetBoolean(json, "ignoreNBT", ignoreNBT);
		
		requiredItems.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "requiredItems"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			try
			{
				ItemStack item = ItemStack.loadItemStackFromNBT(NBTConverter.JSONtoNBT_Object(entry.getAsJsonObject(), new NBTTagCompound()));
				
				if(item != null)
				{
					requiredItems.add(item);
				} else
				{
					continue;
				}
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to load quest item data", e);
			}
		}
	}

	@Override
	public void drawQuestInfo(GuiQuesting screen, int mx, int my, int posX, int posY, int sizeX, int sizeY)
	{
		ItemStack ttStack = null;
		
		for(int i = 0; i < requiredItems.size(); i++)
		{
			ItemStack stack = requiredItems.get(i);
			screen.mc.renderEngine.bindTexture(GuiQuesting.guiTexture);
			screen.drawTexturedModalRect(posX + (i * 18), posY, 0, 48, 18, 18);
			RenderUtils.RenderItemStack(screen.mc, stack, posX + (i * 18) + 1, posY + 1, stack != null && stack.stackSize > 1? "" + stack.stackSize : "", false);
			
			if(screen.isWithin(mx, my, posX + (i * 18), posY, 16, 16, false))
			{
				ttStack = stack;
			}
		}
		
		if(this.isComplete(screen.mc.thePlayer))
		{
			screen.drawString(screen.mc.fontRenderer, ChatFormatting.BOLD + "COMPLETE", posX, posY + 24, Color.GREEN.getRGB());
		} else
		{
			screen.drawString(screen.mc.fontRenderer, ChatFormatting.BOLD + "INCOMPLETE", posX, posY + 24, Color.RED.getRGB());
		}
		
		if(ttStack != null)
		{
			screen.DrawTooltip(ttStack.getTooltip(screen.mc.thePlayer, screen.mc.gameSettings.advancedItemTooltips), mx, my);
		}
	}
}
