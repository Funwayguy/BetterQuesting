package betterquesting.quests.tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.core.BetterQuesting;
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

public class TaskFluid extends TaskBase
{
	public int scroll = 0;
	public ArrayList<FluidStack> requiredFluids = new ArrayList<FluidStack>();
	public HashMap<UUID, int[]> userProgress = new HashMap<UUID, int[]>();
	public boolean consume = true;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.task.fluid";
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
		if(!player.isEntityAlive() || player.inventory == null || this.isComplete(player) || requiredFluids.size() <= 0)
		{
			return;
		}
		
		boolean flag = true;
		
		int[] progress = userProgress.get(player.getUniqueID());
		progress = progress == null || progress.length != requiredFluids.size()? new int[requiredFluids.size()] : progress;
		
		for(int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			for(int j = 0; j < requiredFluids.size(); j++)
			{
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack == null || FluidContainerRegistry.isEmptyContainer(stack))
				{
					break;
				}
				
				FluidStack rStack = requiredFluids.get(j);
				
				if(rStack == null || progress[j] >= rStack.amount)
				{
					continue;
				}
				
				int remaining = rStack.amount - progress[j];
				
				if(rStack.isFluidEqual(stack))
				{
					if(consume)
					{
						FluidStack fluid = this.getFluid(player, i, true, remaining);
						progress[j] += Math.min(remaining, fluid == null? 0 : fluid.amount);
					} else
					{
						FluidStack fluid = this.getFluid(player, i, false, remaining);
						progress[j] += Math.min(remaining, fluid == null? 0 : fluid.amount);
					}
				}
			}
		}
		
		for(int j = 0; j < requiredFluids.size(); j++)
		{
			FluidStack rStack = requiredFluids.get(j);
			
			if(rStack == null || progress[j] >= rStack.amount)
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
	
	/**
	 * Returns the fluid drained (or can be drained) up to the specified amount
	 */
	public FluidStack getFluid(EntityPlayer player, int slot, boolean drain, int amount)
	{
		ItemStack stack = player.inventory.getStackInSlot(slot);
		
		if(stack == null || amount <= 0)
		{
			return null;
		}
		
		if(stack.getItem() instanceof IFluidContainerItem)
		{
			IFluidContainerItem container = (IFluidContainerItem)stack.getItem();
			
			return container.drain(stack, amount, drain);
			
		} else
		{
			FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
			int tmp1 = fluid.amount;
			int tmp2 = 1;
			while(fluid.amount < amount && tmp2 < stack.stackSize)
			{
				tmp2++;
				fluid.amount += tmp1;
			}
			
			if(drain)
			{
				for(; tmp2 > 0; tmp2--)
				{
					ItemStack empty = FluidContainerRegistry.drainFluidContainer(stack);
					player.inventory.decrStackSize(slot, 1);
					
					if(!player.inventory.addItemStackToInventory(empty))
					{
						player.dropPlayerItemWithRandomChoice(empty, false);
					}
				}
			}
			
			return fluid;
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("consume", consume);
		
		JsonArray itemArray = new JsonArray();
		for(FluidStack stack : this.requiredFluids)
		{
			itemArray.add(NBTConverter.NBTtoJSON_Compound(stack.writeToNBT(new NBTTagCompound()), new JsonObject()));
		}
		json.add("requiredFluids", itemArray);
		
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
		
		consume = JsonHelper.GetBoolean(json, "consume", true);
		
		requiredFluids.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "requiredFluids"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			FluidStack fluid = FluidStack.loadFluidStackFromNBT(NBTConverter.JSONtoNBT_Object(entry.getAsJsonObject(), new NBTTagCompound()));
			
			if(fluid != null)
			{
				requiredFluids.add(fluid);
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
			
			int[] data = new int[requiredFluids.size()];
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
		int rowL = Math.min(requiredFluids.size(), rowLMax);
		
		if(rowLMax < requiredFluids.size())
		{
			scroll = MathHelper.clamp_int(scroll, 0, requiredFluids.size() - rowLMax);
			RenderUtils.DrawFakeButton(screen, posX, posY, 20, 20, "<", screen.isWithin(mx, my, posX, posY, 20, 20, false)? 2 : 1);
			RenderUtils.DrawFakeButton(screen, posX + 20 + 18*rowL, posY, 20, 20, ">", screen.isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20, false)? 2 : 1);
		} else
		{
			scroll = 0;
		}
		
		FluidStack ttStack = null;
		String ttAmount = "0/0 mB";
		
		int[] progress = userProgress.get(screen.mc.thePlayer.getUniqueID());
		progress = progress == null? new int[requiredFluids.size()] : progress;
		
		for(int i = 0; i < rowL; i++)
		{
			FluidStack stack = requiredFluids.get(i + scroll);
			screen.mc.renderEngine.bindTexture(GuiQuesting.guiTexture);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			screen.drawTexturedModalRect(posX + (i * 18) + 20, posY, 0, 48, 18, 18);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			int count = stack.amount - progress[i + scroll];
			
			if(stack != null)
			{
				screen.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				if(progress[i] >= stack.amount)
				{
					GL11.glColor4f(1F, 1F, 1F, 1F);
					RenderUtils.itemRender.renderIcon(posX + (i * 18) + 21, posY + 1, stack.getFluid().getIcon(), 16, 16);
				} else
				{
					GL11.glColor4f(1F, 1F, 1F, 0.25F);
					RenderUtils.itemRender.renderIcon(posX + (i * 18) + 21, posY + 1, stack.getFluid().getIcon(), 16, 16);
					GL11.glColor4f(1F, 1F, 1F, 1F);
					RenderUtils.itemRender.renderIcon(posX + (i * 18) + 21, posY + 1 - MathHelper.floor_float(16 * (progress[i]/(float)stack.amount) - 16), stack.getFluid().getIcon(), 16, MathHelper.floor_float(16 * (progress[i]/(float)stack.amount)));
				}
			}
			
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
				ttAmount = progress[i] + "/" + stack.amount + " mB";
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
			ArrayList<String> tTip = new ArrayList<String>();
			tTip.add(ttStack.getLocalizedName());
			tTip.add(ChatFormatting.GRAY + ttAmount);
			screen.DrawTooltip(tTip, mx, my);
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
		int rowL = Math.min(requiredFluids.size(), rowLMax);
		
		if(screen.isWithin(mx, my, posX, posY, 20, 20, false))
		{
			scroll = MathHelper.clamp_int(scroll - 1, 0, requiredFluids.size() - rowLMax);
		} else if(screen.isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20, false))
		{
			scroll = MathHelper.clamp_int(scroll + 1, 0, requiredFluids.size() - rowLMax);
		}
	}
}
