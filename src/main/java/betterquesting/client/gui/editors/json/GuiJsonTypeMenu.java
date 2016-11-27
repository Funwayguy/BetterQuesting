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
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.client.gui.editors.json.callback.JsonEntityCallback;
import betterquesting.client.gui.editors.json.callback.JsonFluidCallback;
import betterquesting.client.gui.editors.json.callback.JsonItemCallback;
import betterquesting.client.gui.editors.json.scrolling.GuiJsonEditor;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiJsonTypeMenu extends GuiScreenThemed implements IVolatileScreen
{
	private final JsonObject json;
	private FluidStack fluid;
	private BigItemStack stack;
	private Entity entity;
	private EditType lastType = EditType.NONE;
	
	private JsonItemCallback itemCallback;
	private JsonFluidCallback fluidCallback;
	private JsonEntityCallback entityCallback;
	
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
		
		fluid = null;
		stack = null;
		entity = null;
		
		if(json != null)
		{
			if(JsonHelper.isItem(json)) // Must have at least these 3 to be considered a valid 'item'
			{
				stack = JsonHelper.JsonToItemStack(json);
			}
			
			if(stack == null && JsonHelper.isEntity(json))
			{
				entity = EntityList.createEntityFromNBT(NBTConverter.JSONtoNBT_Object(json.getAsJsonObject(), new NBTTagCompound()), Minecraft.getMinecraft().theWorld);
			}
			
			if(stack == null && entity == null && JsonHelper.isFluid(json))
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
			JsonHelper.FluidStackToJson(fluid, json);
		} else if(lastType == EditType.ENTITY)
		{
			json.entrySet().clear();
			JsonHelper.EntityToJson(entity, json);
		}
		
		if(lastType != EditType.NONE)
		{
			mc.displayGuiScreen(parent);
		}

		GuiButtonThemed editButton = new GuiButtonThemed(3, this.width/2 - 100, this.height/2 - 40, 200, 20, I18n.format("betterquesting.btn.raw_nbt"), true); // JSON Editor
		GuiButtonThemed itemButton = new GuiButtonThemed(1, this.width/2 - 100, this.height/2 - 20, 200, 20, I18n.format("betterquesting.btn.item"), true); // Item Selector
		GuiButtonThemed fluidButton = new GuiButtonThemed(4, this.width/2 - 100, this.height/2 + 00, 200, 20, I18n.format("betterquesting.btn.fluid"), true); // Fluid Editor
		GuiButtonThemed entityButton = new GuiButtonThemed(2, this.width/2 - 100, this.height/2 + 20, 200, 20, I18n.format("betterquesting.btn.entity"), true); // Entity Selector
		
		this.buttonList.add(itemButton);
		this.buttonList.add(entityButton);
		this.buttonList.add(editButton);
		this.buttonList.add(fluidButton);
		
		itemCallback = new JsonItemCallback(json, stack);
		fluidCallback = new JsonFluidCallback(json, fluid);
		entityCallback = new JsonEntityCallback(json, entity);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		if(button.id == 1)
		{
			this.lastType = EditType.ITEM;
			this.mc.displayGuiScreen(new GuiJsonItemSelection(this, itemCallback, stack));
		} else if(button.id == 2)
		{
			this.lastType = EditType.ENTITY;
			this.mc.displayGuiScreen(new GuiJsonEntitySelection(this, entityCallback, entity));
		} else if(button.id == 3)
		{
			this.lastType = EditType.NONE;
			this.mc.displayGuiScreen(new GuiJsonEditor(this, json, null));
		} else if(button.id == 4)
		{
			this.lastType = EditType.FLUID;
			this.mc.displayGuiScreen(new GuiJsonFluidSelection(this, fluidCallback, fluid));
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
