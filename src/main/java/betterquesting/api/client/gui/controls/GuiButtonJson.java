package betterquesting.api.client.gui.controls;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;

public class GuiButtonJson<T extends NBTBase> extends GuiButtonStorage<T>
{
	private T json = null;
	
	private boolean isItem = false;
	private boolean isFluid = false;
	private boolean isEntity = false;
	
	public GuiButtonJson(int id, int posX, int posY, T json)
	{
		this(id, posX, posY, 200, 20, json, true);
	}
	
	public GuiButtonJson(int id, int posX, int posY, int width, int height, T json, boolean shadow)
	{
		super(id, posX, posY, width, height, "", shadow);
		this.json = json;
		this.refreshJson();
	}
	
	@Override
	public T getStored()
	{
		return json;
	}
	
	@Override
	public void setStored(T value)
	{
		this.json = value;
	}
	
	public void refreshJson()
	{
		if(json == null)
		{
			this.displayString = "?";
			return;
		} else if(json.getId() == 10)
		{
			NBTTagCompound jcTag = (NBTTagCompound)json;
			if(JsonHelper.isItem(jcTag))
			{
				isItem = true;
			} else if(JsonHelper.isFluid(jcTag))
			{
				isFluid = true;
			} else if(JsonHelper.isEntity(jcTag))
			{
				isEntity = true;
			}
		}
		
		if(isItem)
		{
			BigItemStack stack = JsonHelper.JsonToItemStack((NBTTagCompound)json);
			this.displayString = I18n.format("betterquesting.btn.item") + ": " + stack.getBaseStack().getDisplayName();
		} else if(isFluid)
		{
			FluidStack fluid = JsonHelper.JsonToFluidStack((NBTTagCompound)json);
			this.displayString = I18n.format("betterquesting.btn.fluid") + ": " + fluid.getLocalizedName();
		} else if(isEntity)
		{
			Entity entity = JsonHelper.JsonToEntity((NBTTagCompound)json, this.mc.theWorld);
			this.displayString = I18n.format("betterquesting.btn.entity") + ": " + entity.getName();
		} else
		{
			this.displayString = getJsonName();
		}
	}
	
	private String getJsonName()
	{
		if(json == null)
		{
			return "?";
		} else if(json.getId() == 10)
		{
			return I18n.format("betterquesting.btn.object") + "...";
		} else if(json.getId() == 9)
		{
			return I18n.format("betterquesting.btn.list") + "...";
		}/* else if(json.getId() == 1)
		{
			NBTTagByte jPrim = (NBTTagByte)json;
			
			if(jPrim.getByte() > 0)
			{
				return "true";
			} else
			{
				return I18n.format("betterquesting.btn.text"); // An editable text field should have been used
			}
		}*/
		
		return json.getClass().getSimpleName();
	}
	
	public boolean isItem()
	{
		return isItem;
	}
	
	public boolean isFluid()
	{
		return isFluid;
	}
	
	public boolean isEntity()
	{
		return isEntity;
	}
}
