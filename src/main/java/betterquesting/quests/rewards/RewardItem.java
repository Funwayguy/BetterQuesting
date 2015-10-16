package betterquesting.quests.rewards;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.Level;
import betterquesting.client.GuiQuesting;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RewardItem extends RewardBase
{
	public ArrayList<ItemStack> rewards = new ArrayList<ItemStack>();
	
	@Override
	public boolean canClaim(EntityPlayer player)
	{
		return true;
	}

	@Override
	public void Claim(EntityPlayer player)
	{
		for(ItemStack r : rewards)
		{
			ItemStack stack = r.copy();
			
			if(!player.inventory.addItemStackToInventory(stack))
			{
				player.dropPlayerItemWithRandomChoice(stack, false);
			}
		}
	}

	@Override
	public void readFromJson(JsonObject json)
	{
		rewards.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "rewards"))
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
					rewards.add(item);
				} else
				{
					continue;
				}
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to load reward item data", e);
			}
		}
	}

	@Override
	public void writeToJson(JsonObject json)
	{
		JsonArray rJson = new JsonArray();
		for(ItemStack stack : rewards)
		{
			rJson.add(NBTConverter.NBTtoJSON_Compound(stack.writeToNBT(new NBTTagCompound()), new JsonObject()));
		}
		json.add("rewards", rJson);
	}

	@Override
	public void drawReward(GuiQuesting screen, int mx, int my, int posX, int posY, int sizeX, int sizeY)
	{
		ItemStack ttStack = null; // Reset
		
		for(int i = 0; i < rewards.size(); i++)
		{
			ItemStack stack = rewards.get(i);
			screen.mc.renderEngine.bindTexture(GuiQuesting.guiTexture);
			screen.drawTexturedModalRect(posX + (i * 18), posY, 0, 48, 18, 18);
			RenderUtils.RenderItemStack(screen.mc, stack, posX + (i * 18) + 1, posY + 1, stack != null && stack.stackSize > 1? "" + stack.stackSize : "", false);
			
			if(screen.isWithin(mx, my, posX + (i * 18), posY, 16, 16, false))
			{
				ttStack = stack;
			}
		}
		
		if(ttStack != null)
		{
			screen.DrawTooltip(ttStack.getTooltip(screen.mc.thePlayer, screen.mc.gameSettings.advancedItemTooltips), mx, my);
		}
	}
}
