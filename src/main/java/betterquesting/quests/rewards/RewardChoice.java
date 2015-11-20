package betterquesting.quests.rewards;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RewardChoice extends RewardBase
{
	public int scroll = 0;
	public int selected = -1;
	public ArrayList<BigItemStack> choices = new ArrayList<BigItemStack>();
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.reward.choice";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, NBTTagCompound choiceData)
	{
		return choices.size() <= 0 || (selected >= 0 && selected < choices.size());
	}

	@Override
	public void Claim(EntityPlayer player, NBTTagCompound choiceData)
	{
		if(choices.size() <= 0)
		{
			return;
		}
		
		selected = choiceData.hasKey("selected")? choiceData.getInteger("selected") : -1;
		
		if(selected < 0 || selected >= choices.size())
		{
			BetterQuesting.logger.log(Level.ERROR, "Choice reward was forcibly claimed with invalid choice", new IllegalStateException());
			return;
		}
		
		BigItemStack stack = choices.get(selected);
		stack = stack == null? null : stack.copy();
		
		if(stack == null || stack.stackSize <= 0)
		{
			BetterQuesting.logger.log(Level.WARN, "Claimed reward choice was null or was 0 in size!");
			return;
		}
		
		for(ItemStack s : stack.getCombinedStacks())
		{
			if(!player.inventory.addItemStackToInventory(s))
			{
				player.dropPlayerItemWithRandomChoice(s, false);
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public NBTTagCompound GetChoiceData()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("selected", selected);
		return tag;
	}

	@Override
	public void readFromJson(JsonObject json)
	{
		choices.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "choices"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			BigItemStack item = JsonHelper.JsonToItemStack(entry.getAsJsonObject());
			
			if(item != null)
			{
				choices.add(item);
			} else
			{
				continue;
			}
		}
	}

	@Override
	public void writeToJson(JsonObject json)
	{
		JsonArray rJson = new JsonArray();
		for(BigItemStack stack : choices)
		{
			rJson.add(JsonHelper.ItemStackToJson(stack, new JsonObject()));
		}
		json.add("choices", rJson);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawReward(GuiQuesting screen, int mx, int my, int posX, int posY, int sizeX, int sizeY)
	{
		int rowLMax = (sizeX - 40)/18;
		int rowL = Math.min(choices.size(), rowLMax);
		
		if(rowLMax < choices.size())
		{
			scroll = MathHelper.clamp_int(scroll, 0, choices.size() - rowLMax);
			RenderUtils.DrawFakeButton(screen, posX, posY, 20, 20, "<", screen.isWithin(mx, my, posX, posY, 20, 20, false)? 2 : 1);
			RenderUtils.DrawFakeButton(screen, posX + 20 + 18*rowL, posY, 20, 20, ">", screen.isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20, false)? 2 : 1);
		} else
		{
			scroll = 0;
		}
		
		BigItemStack ttStack = null; // Reset
		
		for(int i = 0; i < rowL; i++)
		{
			BigItemStack stack = choices.get(i + scroll);
			screen.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			screen.drawTexturedModalRect(posX + (i * 18) + 20, posY + 1, 0, 48, 18, 18);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderUtils.RenderItemStack(screen.mc, stack.getBaseStack(), posX + (i * 18) + 21, posY + 2, stack != null && stack.stackSize > 1? "" + stack.stackSize : "");
			
			if(screen.isWithin(mx, my, posX + (i * 18) + 20, posY + 1, 16, 16, false))
			{
				ttStack = stack;
			}
		}
		
		screen.mc.fontRenderer.drawString("Selected:", posX, posY + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		screen.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		screen.drawTexturedModalRect(posX + 50, posY + 28, 0, 48, 18, 18);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		BigItemStack stack = (selected >= 0 && selected < choices.size())? choices.get(selected) : null;
		
		if(stack != null)
		{
			RenderUtils.RenderItemStack(screen.mc, stack.getBaseStack(), posX + 51, posY + 29, stack != null && stack.stackSize > 1? "" + stack.stackSize : "");
			
			if(screen.isWithin(mx, my, posX + 51, posY + 29, 16, 16, false))
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
		int rowL = Math.min(choices.size(), rowLMax);
		
		if(screen.isWithin(mx, my, posX + 20, posY + 2, rowL * 18, 18, false))
		{
			int i = (mx - posX - 20)/18;
			selected = i + scroll;
		} else if(screen.isWithin(mx, my, posX, posY, 20, 20, false))
		{
			scroll = MathHelper.clamp_int(scroll - 1, 0, choices.size() - rowLMax);
		} else if(screen.isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20, false))
		{
			scroll = MathHelper.clamp_int(scroll + 1, 0, choices.size() - rowLMax);
		}
	}
}
