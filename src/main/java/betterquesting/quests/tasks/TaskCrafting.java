package betterquesting.quests.tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.quests.tasks.advanced.AdvancedTaskBase;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.ItemComparison;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.RenderUtils;

public class TaskCrafting extends AdvancedTaskBase
{
	public ArrayList<BigItemStack> requiredItems = new ArrayList<BigItemStack>();
	public HashMap<UUID, int[]> userProgress = new HashMap<UUID, int[]>();
	boolean partialMatch = true;
	boolean ignoreNBT = false;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.task.crafting";
	}
	
	@Override
	public void onItemCrafted(EntityPlayer player, ItemStack stack)
	{
		int[] progress = userProgress.get(player.getUniqueID());
		progress = progress == null || progress.length != requiredItems.size()? new int[requiredItems.size()] : progress;
		
		boolean flag = true;
		
		for(int i = 0; i < requiredItems.size(); i++)
		{
			BigItemStack rStack = requiredItems.get(i);
			
			if(progress[i] >= rStack.stackSize)
			{
				continue;
			}
			
			if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch))
			{
				progress[i] += stack.stackSize;
			}
			
			if(progress[i] < rStack.stackSize)
			{
				flag = false;
			}
		}
		
		userProgress.put(player.getUniqueID(), progress);
		
		if(flag)
		{
			this.completeUsers.add(player.getUniqueID());
		}
	}
	
	@Override
	public void onItemSmelted(EntityPlayer player, ItemStack stack)
	{
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void drawQuestInfo(GuiQuesting screen, int mx, int my, int posX, int posY, int sizeX, int sizeY)
	{
		BigItemStack ttStack = null;
		
		int[] progress = userProgress.get(screen.mc.thePlayer.getUniqueID());
		progress = progress == null? new int[requiredItems.size()] : progress;
		
		for(int i = 0; i < requiredItems.size(); i++)
		{
			BigItemStack stack = requiredItems.get(i);
			screen.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			screen.drawTexturedModalRect(posX + (i * 18), posY, 0, 48, 18, 18);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			int count = stack.stackSize - progress[i];
			
			RenderUtils.RenderItemStack(screen.mc, stack.getBaseStack(), posX + (i * 18) + 1, posY + 1, stack != null && stack.stackSize > 1? "" + count : "");
			
			if(count <= 0 || this.isComplete(screen.mc.thePlayer))
			{
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				screen.mc.fontRenderer.drawString("\u2714", posX + (i * 18) + 6, posY + 6, Color.BLACK.getRGB(), false);
				screen.mc.fontRenderer.drawString("\u2714", posX + (i * 18) + 5, posY + 5, Color.GREEN.getRGB(), false);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
			
			if(screen.isWithin(mx, my, posX + (i * 18), posY, 16, 16, false))
			{
				ttStack = stack;
			}
		}
		
		if(this.isComplete(screen.mc.thePlayer))
		{
			screen.mc.fontRenderer.drawString(ChatFormatting.BOLD + "COMPLETE", posX, posY + 24, Color.GREEN.getRGB(), false);
		} else
		{
			screen.mc.fontRenderer.drawString(ChatFormatting.BOLD + "INCOMPLETE", posX, posY + 24, Color.RED.getRGB(), false);
		}
		
		if(ttStack != null)
		{
			screen.DrawTooltip(ttStack.getBaseStack().getTooltip(screen.mc.thePlayer, screen.mc.gameSettings.advancedItemTooltips), mx, my);
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("partialMatch", partialMatch);
		json.addProperty("ignoreNBT", ignoreNBT);
		
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
		
		requiredItems.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "requiredItems"))
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
			
			int[] data = new int[requiredItems.size()];
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
}
