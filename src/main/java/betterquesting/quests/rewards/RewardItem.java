package betterquesting.quests.rewards;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.gui.rewards.GuiRewardItem;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RewardItem extends RewardBase
{
	public ArrayList<BigItemStack> items = new ArrayList<BigItemStack>();
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.reward.item";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, NBTTagCompound choiceData)
	{
		return true;
	}

	@Override
	public void Claim(EntityPlayer player, NBTTagCompound choiceData)
	{
		for(BigItemStack r : items)
		{
			BigItemStack stack = r.copy();
			
			for(ItemStack s : stack.getCombinedStacks())
			{
				if(!player.inventory.addItemStackToInventory(s))
				{
					player.dropPlayerItemWithRandomChoice(s, false);
				}
			}
		}
	}

	@Override
	public void readFromJson(JsonObject json)
	{
		items.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "rewards"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			try
			{
				BigItemStack item = JsonHelper.JsonToItemStack(entry.getAsJsonObject());
				
				if(item != null)
				{
					items.add(item);
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
		for(BigItemStack stack : items)
		{
			rJson.add(JsonHelper.ItemStackToJson(stack, new JsonObject()));
		}
		json.add("rewards", rJson);
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiRewardItem(this, screen, posX, posY, sizeX, sizeY);
	}
}
