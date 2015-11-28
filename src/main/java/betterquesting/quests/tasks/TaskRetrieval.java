package betterquesting.quests.tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.ItemComparison;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TaskRetrieval extends TaskBase
{
	public int scroll = 0;
	public ArrayList<BigItemStack> requiredItems = new ArrayList<BigItemStack>();
	public HashMap<UUID, int[]> userProgress = new HashMap<UUID, int[]>();
	boolean partialMatch = true;
	boolean ignoreNBT = false;
	public boolean consume = true;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.task.retrieval";
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
		if(!player.isEntityAlive() || player.inventory == null || this.isComplete(player) || requiredItems.size() <= 0)
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
	@SideOnly(Side.CLIENT)
	public void drawQuestInfo(GuiQuesting screen, int mx, int my, int posX, int posY, int sizeX, int sizeY)
	{
		int rowLMax = (sizeX - 40)/18;
		int rowL = Math.min(requiredItems.size(), rowLMax);
		
		if(rowLMax < requiredItems.size())
		{
			scroll = MathHelper.clamp_int(scroll, 0, requiredItems.size() - rowLMax);
			RenderUtils.DrawFakeButton(screen, posX, posY, 20, 20, "<", screen.isWithin(mx, my, posX, posY, 20, 20, false)? 2 : 1);
			RenderUtils.DrawFakeButton(screen, posX + 20 + 18*rowL, posY, 20, 20, ">", screen.isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20, false)? 2 : 1);
		} else
		{
			scroll = 0;
		}
		
		BigItemStack ttStack = null;
		
		int[] progress = userProgress.get(screen.mc.thePlayer.getUniqueID());
		progress = progress == null? new int[requiredItems.size()] : progress;
		
		for(int i = 0; i < rowL; i++)
		{
			BigItemStack stack = requiredItems.get(i + scroll);
			screen.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			screen.drawTexturedModalRect(posX + (i * 18) + 20, posY, 0, 48, 18, 18);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			int count = stack.stackSize - progress[i + scroll];
			
			RenderUtils.RenderItemStack(screen.mc, stack.getBaseStack(), posX + (i * 18) + 21, posY + 1, stack != null && stack.stackSize > 1? "" + count : "");
			
			if(count <= 0 || this.isComplete(screen.mc.thePlayer))
			{
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				// Shadows don't work on these symbols for some reason so we manually draw a shadow
				screen.mc.fontRenderer.drawString("\u2714", posX + (i * 18) + 26, posY + 6, Color.BLACK.getRGB(), false);
				screen.mc.fontRenderer.drawString("\u2714", posX + (i * 18) + 25, posY + 5, Color.GREEN.getRGB(), false);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
			
			if(screen.isWithin(mx, my, posX + (i * 18) + 21, posY, 16, 16, false))
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
	@SideOnly(Side.CLIENT)
	public void MousePressed(GuiQuesting screen, int mx, int my, int posX, int posY, int sizeX, int sizeY, int click)
	{
		if(click != 0)
		{
			return;
		}
		
		int rowLMax = (sizeX - 40)/18;
		int rowL = Math.min(requiredItems.size(), rowLMax);
		
		if(screen.isWithin(mx, my, posX, posY, 20, 20, false))
		{
			scroll = MathHelper.clamp_int(scroll - 1, 0, requiredItems.size() - rowLMax);
		} else if(screen.isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20, false))
		{
			scroll = MathHelper.clamp_int(scroll + 1, 0, requiredItems.size() - rowLMax);
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
