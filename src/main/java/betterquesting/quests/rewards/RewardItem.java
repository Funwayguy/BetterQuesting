package betterquesting.quests.rewards;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RewardItem extends RewardBase
{
	public int scroll = 0;
	public ArrayList<BigItemStack> rewards = new ArrayList<BigItemStack>();
	
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
		for(BigItemStack r : rewards)
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
		rewards.clear();
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
		for(BigItemStack stack : rewards)
		{
			rJson.add(JsonHelper.ItemStackToJson(stack, new JsonObject()));
		}
		json.add("rewards", rJson);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawReward(GuiQuesting screen, int mx, int my, int posX, int posY, int sizeX, int sizeY)
	{
		int rowLMax = (sizeX - 40)/18;
		int rowL = Math.min(rewards.size(), rowLMax);
		
		if(rowLMax < rewards.size())
		{
			scroll = MathHelper.clamp_int(scroll, 0, rewards.size() - rowLMax);
			RenderUtils.DrawFakeButton(screen, posX, posY, 20, 20, "<", screen.isWithin(mx, my, posX, posY, 20, 20, false)? 2 : 1);
			RenderUtils.DrawFakeButton(screen, posX + 20 + 18*rowL, posY, 20, 20, ">", screen.isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20, false)? 2 : 1);
		} else
		{
			scroll = 0;
		}
		
		BigItemStack ttStack = null; // Reset
		
		for(int i = 0; i < rowL; i++)
		{
			BigItemStack stack = rewards.get(i + scroll);
			screen.mc.renderEngine.bindTexture(GuiQuesting.guiTexture);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			screen.drawTexturedModalRect(posX + (i * 18) + 20, posY + 1, 0, 48, 18, 18);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderUtils.RenderItemStack(screen.mc, stack.getBaseStack(), posX + (i * 18) + 21, posY + 2, stack != null && stack.stackSize > 1? "" + stack.stackSize : "");
			
			if(screen.isWithin(mx, my, posX + (i * 18) + 20, posY + 1, 16, 16, false))
			{
				ttStack = stack;
			}
		}
		
		if(ttStack != null)
		{
			screen.DrawTooltip(ttStack.getBaseStack().getTooltip(screen.mc.thePlayer, screen.mc.gameSettings.advancedItemTooltips), mx, my);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void MousePressed(GuiQuesting screen, int mx, int my, int posX, int posY, int sizeX, int sizeY, int click)
	{
		if(click != 0)
		{
			return;
		}
		
		int rowLMax = (sizeX - 40)/18;
		int rowL = Math.min(rewards.size(), rowLMax);
		
		if(screen.isWithin(mx, my, posX, posY, 20, 20, false))
		{
			scroll = MathHelper.clamp_int(scroll - 1, 0, rewards.size() - rowLMax);
		} else if(screen.isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20, false))
		{
			scroll = MathHelper.clamp_int(scroll + 1, 0, rewards.size() - rowLMax);
		}
	}
}
