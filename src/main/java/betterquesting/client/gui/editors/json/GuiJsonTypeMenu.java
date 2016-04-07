package betterquesting.client.gui.editors.json;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiJsonTypeMenu extends GuiQuesting implements IVolatileScreen
{
	JsonObject json;
	FluidStack fluid;
	BigItemStack stack;
	Entity entity;
	EditType lastType = EditType.NONE;
	
	public GuiJsonTypeMenu(GuiScreen parent, JsonObject json)
	{
		super(parent, "betterquesting.title.json_object");
		this.json = json;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		if(json != null)
		{
			if(json.has("id") && json.has("Damage") && json.has("Count")) // Must have at least these 3 to be considered a valid 'item'
			{
				stack = JsonHelper.JsonToItemStack(json);
			}
			
			if(stack == null && json.has("id") && EntityList.stringToClassMapping.get(JsonHelper.GetString(json, "id", "Pig")) != null)
			{
				entity = EntityList.createEntityFromNBT(NBTConverter.JSONtoNBT_Object(json.getAsJsonObject(), new NBTTagCompound()), Minecraft.getMinecraft().theWorld);
			}
			
			if(stack == null && entity == null && json.has("FluidName") && json.has("Amount"))
			{
				fluid = JsonHelper.JsonToFluidStack(json);
			}
		} else // JSON cannot be null!
		{
			this.mc.displayGuiScreen(parent);
			return;
		}
		
		if(stack == null)
		{
			stack = new BigItemStack(Blocks.stone);
		}
		
		if(entity == null)
		{
			entity = new EntityPig(Minecraft.getMinecraft().theWorld);
		}
		
		if(fluid == null)
		{
			fluid = new FluidStack(FluidRegistry.WATER, 1000);
		}
		
		if(lastType == EditType.ITEM)
		{
			json.entrySet().clear();
			JsonHelper.ItemStackToJson(stack, json);
		} else if(lastType == EditType.FLUID)
		{
			json.entrySet().clear();
			NBTConverter.NBTtoJSON_Compound(fluid.writeToNBT(new NBTTagCompound()), json);
		} else if(lastType == EditType.ENTITY)
		{
			try
			{
				NBTTagCompound eTags = new NBTTagCompound();
				entity.writeToNBTOptional(eTags);
				json.entrySet().clear();
				NBTConverter.NBTtoJSON_Compound(eTags, json);
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "An error occured while reading JSON entity", e);
			}
		}

		GuiButtonQuesting editButton = new GuiButtonQuesting(3, this.width/2 - 100, this.height/2 - 40, 200, 20, I18n.format("betterquesting.btn.raw_nbt")); // JSON Editor
		GuiButtonQuesting itemButton = new GuiButtonQuesting(1, this.width/2 - 100, this.height/2 - 20, 200, 20, I18n.format("betterquesting.btn.item")); // Item Selector
		GuiButtonQuesting fluidButton = new GuiButtonQuesting(4, this.width/2 - 100, this.height/2 + 00, 200, 20, I18n.format("betterquesting.btn.fluid")); // Fluid Editor
		GuiButtonQuesting entityButton = new GuiButtonQuesting(2, this.width/2 - 100, this.height/2 + 20, 200, 20, I18n.format("betterquesting.btn.entity")); // Entity Selector
		
		//itemButton.displayString = I18n.format("betterquesting.btn.item");
		//entityButton.displayString = I18n.format("betterquesting.btn.entity");
		//editButton.displayString = I18n.format("betterquesting.btn.raw_nbt");
		//fluidButton.displayString = I18n.format("betterquesting.btn.fluid");
		
		this.buttonList.add(itemButton);
		this.buttonList.add(entityButton);
		this.buttonList.add(editButton);
		this.buttonList.add(fluidButton);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		if(button.id == 1)
		{
			this.lastType = EditType.ITEM;
			json.entrySet().clear();
			JsonHelper.ItemStackToJson(stack, json);
			
			this.mc.displayGuiScreen(new GuiJsonItemSelection(this, json));
		} else if(button.id == 2)
		{
			this.lastType = EditType.ENTITY;
			json.entrySet().clear();
			NBTTagCompound eTags = new NBTTagCompound();
			entity.writeToNBTOptional(eTags);
			NBTConverter.NBTtoJSON_Compound(eTags, json);
			
			this.mc.displayGuiScreen(new GuiJsonEntitySelection(this, json));
		} else if(button.id == 3)
		{
			this.lastType = EditType.NONE;
			this.mc.displayGuiScreen(new GuiJsonObject(this, json));
		} else if(button.id == 4)
		{
			this.lastType = EditType.FLUID;
			json.entrySet().clear();
			NBTConverter.NBTtoJSON_Compound(fluid.writeToNBT(new NBTTagCompound()), json);
			
			this.mc.displayGuiScreen(new GuiJsonFluidSelection(this, json));
		} else
		{
			this.lastType = EditType.NONE;
			super.actionPerformed(button);
		}
	}
	
	enum EditType
	{
		NONE,
		ITEM,
		ENTITY,
		FLUID;
	}
}
