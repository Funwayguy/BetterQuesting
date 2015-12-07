package betterquesting.client.gui.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
			
			if(stack == null && tmpObj.has("id") && EntityList.stringToClassMapping.get(tmpObj.get("id").getAsString()) != null)
			{
				this.entity = EntityList.createEntityFromNBT(NBTConverter.JSONtoNBT_Object(json.getAsJsonObject(), new NBTTagCompound()), Minecraft.getMinecraft().theWorld);
			}
			
			if(tmpObj.has("FluidName") && tmpObj.has("Amount"))
			{
				fluid = FluidStack.loadFluidStackFromNBT(NBTConverter.JSONtoNBT_Object(tmpObj, new NBTTagCompound()));
			}
		}
		
		if(this.entity != null)
		{
			this.displayString = I18n.format("betterquesting.btn.entity") + ":" + entity.getCommandSenderName();
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
	
	public boolean isItemStack()
	{
		return stack != null;
	}
	
	public boolean isEntity()
	{
		return entity != null;
	}

	public boolean isFluid()
	{
		return fluid != null;
	}
}
