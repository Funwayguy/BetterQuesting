package betterquesting.quests.rewards;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.client.GuiQuesting;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
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
	public ArrayList<ItemStack> choices = new ArrayList<ItemStack>();
	
	@Override
	public boolean canClaim(EntityPlayer player, NBTTagCompound choiceData)
	{
		return !(selected < 0 || selected >= choices.size());
	}

	@Override
	public void Claim(EntityPlayer player, NBTTagCompound choiceData)
	{
		selected = choiceData.hasKey("selected")? choiceData.getInteger("selected") : -1;
		
		if(selected < 0 || selected >= choices.size())
		{
			BetterQuesting.logger.log(Level.ERROR, "Choice reward was forcibly claimed with invalid choice", new IllegalStateException());
			return;
		}
		
		ItemStack stack = choices.get(selected);
		stack = stack == null? null : stack.copy();
		
		if(stack == null || stack.stackSize <= 0)
		{
			BetterQuesting.logger.log(Level.WARN, "Claimed reward choice was null or was 0 in size!");
			return;
		}
		
		if(!player.inventory.addItemStackToInventory(stack))
		{
			player.dropPlayerItemWithRandomChoice(stack, false);
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
			
			ItemStack item = ItemStack.loadItemStackFromNBT(NBTConverter.JSONtoNBT_Object(entry.getAsJsonObject(), new NBTTagCompound()));
			
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
		for(ItemStack stack : choices)
		{
			rJson.add(NBTConverter.NBTtoJSON_Compound(stack.writeToNBT(new NBTTagCompound()), new JsonObject()));
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
		
		ItemStack ttStack = null; // Reset
		
		for(int i = 0; i < rowL; i++)
		{
			ItemStack stack = choices.get(i + scroll);
			screen.mc.renderEngine.bindTexture(GuiQuesting.guiTexture);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			screen.drawTexturedModalRect(posX + (i * 18) + 20, posY + 1, 0, 48, 18, 18);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderUtils.RenderItemStack(screen.mc, stack, posX + (i * 18) + 21, posY + 2, stack != null && stack.stackSize > 1? "" + stack.stackSize : "", false);
			
			if(screen.isWithin(mx, my, posX + (i * 18) + 20, posY + 1, 16, 16, false))
			{
				ttStack = stack;
			}
		}
		
		screen.mc.fontRenderer.drawString("Selected:", posX, posY + 32, Color.BLACK.getRGB(), false);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		screen.mc.renderEngine.bindTexture(GuiQuesting.guiTexture);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		screen.drawTexturedModalRect(posX + 50, posY + 28, 0, 48, 18, 18);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		ItemStack stack = (selected >= 0 && selected < choices.size())? choices.get(selected) : null;
		
		if(stack != null)
		{
			RenderUtils.RenderItemStack(screen.mc, stack, posX + 51, posY + 29, stack != null && stack.stackSize > 1? "" + stack.stackSize : "", false);
			
			if(screen.isWithin(mx, my, posX + 51, posY + 29, 16, 16, false))
			{
				ttStack = stack;
			}
		}
		
		if(ttStack != null)
		{
			screen.DrawTooltip(ttStack.getTooltip(screen.mc.thePlayer, screen.mc.gameSettings.advancedItemTooltips), mx, my);
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
