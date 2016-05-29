package betterquesting.client.gui.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.json.GuiJsonArray;
import betterquesting.client.gui.editors.json.GuiJsonEntitySelection;
import betterquesting.client.gui.editors.json.GuiJsonFluidSelection;
import betterquesting.client.gui.editors.json.GuiJsonItemSelection;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.client.gui.editors.json.GuiJsonTypeMenu;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@SideOnly(Side.CLIENT)
public class GuiButtonJson extends GuiButtonQuesting
{
	public JsonElement json;
	public BigItemStack stack;
	public Entity entity;
	public FluidStack fluid;
	
	public GuiButtonJson(int index, int posX, int posY, JsonElement json)
	{
		this(index, posX, posY, 200, 20, json);
	}
	
	public GuiButtonJson(int index, int posX, int posY, int sizeX, int sizeY, JsonElement json)
	{
		super(index, posX, posY, sizeX, sizeY, "");
		this.json = json;
		
		if(json.isJsonObject())
		{
			JsonObject tmpObj = json.getAsJsonObject();
			if(tmpObj.has("id") && tmpObj.has("Damage") && tmpObj.has("Count")) // Must have at least these 3 to be considered a valid 'item'
			{
				this.stack = JsonHelper.JsonToItemStack(tmpObj);
			}
			
			if(stack == null && tmpObj.has("id") && EntityList.NAME_TO_CLASS.get(JsonHelper.GetString(tmpObj, "id", "Pig")) != null)
			{
				this.entity = EntityList.createEntityFromNBT(NBTConverter.JSONtoNBT_Object(json.getAsJsonObject(), new NBTTagCompound()), Minecraft.getMinecraft().theWorld);
			}
			
			if(tmpObj.has("FluidName") && tmpObj.has("Amount"))
			{
				fluid = JsonHelper.JsonToFluidStack(tmpObj);
			}
		}
		
		if(this.entity != null)
		{
			this.displayString = I18n.format("betterquesting.btn.entity") + ":" + entity.getName();
		} else if(this.stack != null)
		{
			this.displayString = I18n.format("betterquesting.btn.item") + ": " + stack.getBaseStack().getDisplayName();
		} else if(this.fluid != null)
		{
			this.displayString = I18n.format("betterquesting.btn.fluid") + ": " + fluid.getLocalizedName();
		} else if(json.isJsonPrimitive())
		{
			this.displayString = json.getAsJsonPrimitive().getAsString();
		} else
		{
			this.displayString = GetJsonName(json.getClass()) + "...";
		}
	}
	
	static String GetJsonName(Class<? extends JsonElement> c)
	{
		if(c == JsonObject.class)
		{
			return I18n.format("betterquesting.btn.object");
		} else if(c == JsonArray.class)
		{
			return I18n.format("betterquesting.btn.list");
		} else if(c == JsonPrimitive.class)
		{
			return I18n.format("betterquesting.btn.text"); // This should not normally be seen
		}
		
		return c.getSimpleName();
	}
	
	public GuiScreen getJsonScreen(GuiScreen parent, int mx, int my, boolean allowEdit)
	{
		if(json == null || json.isJsonNull() || json.isJsonPrimitive())
		{
			return null;
		} else if(json.isJsonArray())
		{
			return new GuiJsonArray(parent, json.getAsJsonArray()).SetEditMode(allowEdit);
		} else if(!json.isJsonObject())
		{
			return null;
		} else if(mx >= xPosition + width - Math.min(20, width/2) && mx < xPosition + width && my >= yPosition && my < yPosition + height)
		{
			return new GuiJsonTypeMenu(parent, json.getAsJsonObject());
		} else if(isItemStack())
		{
			return new GuiJsonItemSelection(parent, json.getAsJsonObject());
		} else if(isEntity())
		{
			return new GuiJsonEntitySelection(parent, json.getAsJsonObject());
		} else if(isFluid())
		{
			return new GuiJsonFluidSelection(parent, json.getAsJsonObject());
		} else
		{
			return new GuiJsonObject(parent, json.getAsJsonObject()).SetEditMode(allowEdit);
		}
	}
	
	@Override
	public void drawButton(Minecraft mc, int mx, int my)
	{
		if(json != null && json.isJsonObject() && mc.currentScreen instanceof GuiQuesting)
		{
			int tmp = Math.min(20, width/2);
			int bs = ((GuiQuesting)mc.currentScreen).isWithin(mx, my, xPosition + width - tmp, yPosition, tmp, height, false)? 2 : 1;
			RenderUtils.DrawFakeButton((GuiQuesting)mc.currentScreen, xPosition + width - tmp, yPosition, tmp, height, "...", bs);
			width -= tmp;
			super.drawButton(mc, mx, my);
			width += tmp;
		} else
		{
			super.drawButton(mc, mx, my);
		}
	}
	
	public boolean isItemStack()
	{
		return json != null && json.isJsonObject() && stack != null;
	}
	
	public boolean isEntity()
	{
		return json != null && json.isJsonObject() && entity != null;
	}

	public boolean isFluid()
	{
		return json != null && json.isJsonObject() && fluid != null;
	}
}
