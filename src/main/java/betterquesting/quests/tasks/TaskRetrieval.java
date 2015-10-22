package betterquesting.quests.tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TaskRetrieval extends TaskBase
{
	public ArrayList<ItemStack> requiredItems = new ArrayList<ItemStack>();
	public HashMap<UUID, int[]> userProgress = new HashMap<UUID, int[]>();
	boolean partialMatch = true;
	boolean ignoreNBT = false;
	boolean consume = true;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.quest.retrieval.name";
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
		if(!player.isEntityAlive() || player.inventory == null || this.isComplete(player))
		{
			return;
		}
		
		boolean flag = true;
		
		int[] progress = userProgress.get(player.getUniqueID());
		
		if(progress == null || progress.length != requiredItems.size())
		{
			if(progress != null)
			{
				ArrayUtils.addAll(progress, new int[requiredItems.size()]); // Attempt to retain previous progress. Index position may be lost!
			} else
			{
				progress = new int[requiredItems.size()];
			}
			
			userProgress.put(player.getUniqueID(), progress);
			System.out.println("Adding progress for " + player.getCommandSenderName());
		}
		
		topLoop:
		for(int r = 0; r < requiredItems.size(); r++)
		{
			ItemStack reqStack = requiredItems.get(r);
			int count = consume? progress[r] : 0;
			
			if(count >= reqStack.stackSize)
			{
				continue; // Already complete
			}
			
			for(int i = 0; i < player.inventory.getSizeInventory(); i++)
			{
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack != null)
				{
					if(ItemComparison.StackMatch(reqStack, stack, !ignoreNBT, partialMatch))
					{
						if(consume)
						{
							ItemStack remStack = player.inventory.decrStackSize(i, reqStack.stackSize - count);
							count += remStack != null? remStack.stackSize : 0;
							progress[r] = count;
						} else
						{
							count += stack.stackSize;
						}
						
						if(count >= reqStack.stackSize)
						{
							continue topLoop;
						}
					}
				}
			}
			
			flag = false;
			
			if(!consume)
			{
				break;
			}
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
		for(ItemStack stack : this.requiredItems)
		{
			itemArray.add(NBTConverter.NBTtoJSON_Compound(stack.writeToNBT(new NBTTagCompound()), new JsonObject()));
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
		ItemStack ttStack = null;
		
		int[] progress = userProgress.get(screen.mc.thePlayer.getUniqueID());
		progress = progress == null? new int[requiredItems.size()] : progress;
		
		for(int i = 0; i < requiredItems.size(); i++)
		{
			ItemStack stack = requiredItems.get(i);
			screen.mc.renderEngine.bindTexture(GuiQuesting.guiTexture);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			screen.drawTexturedModalRect(posX + (i * 18), posY, 0, 48, 18, 18);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			int count = stack.stackSize - progress[i];
			
			RenderUtils.RenderItemStack(screen.mc, stack, posX + (i * 18) + 1, posY + 1, stack != null && stack.stackSize > 1? "" + count : "", false);
			
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
			screen.DrawTooltip(ttStack.getTooltip(screen.mc.thePlayer, screen.mc.gameSettings.advancedItemTooltips), mx, my);
		}
	}
}
