package betterquesting.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.client.buttons.GuiButtonJson;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;

public class GuiJsonTypeMenu extends GuiQuesting
{
	JsonObject json;
	ItemStack stack;
	Entity entity;
	EditType lastType = EditType.NONE;
	
	public GuiJsonTypeMenu(GuiScreen parent, JsonObject json)
	{
		super(parent, "Editor - Object");
		this.json = json;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		if(json != null)
		{
			stack = ItemStack.loadItemStackFromNBT(NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
			
			if(json.has("id") && json.has("Damage") && json.has("Count")) // Must have at least these 3 to be considered a valid 'item'
			{
				stack = ItemStack.loadItemStackFromNBT(NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
			}
			
			if(json.has("id") && EntityList.stringToClassMapping.get(json.get("id").getAsString()) != null)
			{
				entity = EntityList.createEntityFromNBT(NBTConverter.JSONtoNBT_Object(json.getAsJsonObject(), new NBTTagCompound()), Minecraft.getMinecraft().theWorld);
			}
		} else // JSON cannot be null!
		{
			this.mc.displayGuiScreen(parent);
			return;
		}
		
		if(stack == null)
		{
			stack = new ItemStack(Blocks.stone);
		}
		
		if(entity == null)
		{
			entity = new EntityPig(Minecraft.getMinecraft().theWorld);
		}
		
		if(lastType == EditType.ITEM)
		{
			json.entrySet().clear();
			NBTConverter.NBTtoJSON_Compound(stack.writeToNBT(new NBTTagCompound()), json);
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
				e.printStackTrace();
			}
		}
		
		GuiButtonJson itemButton = new GuiButtonJson(1, this.width/2 - 100, this.height/2 - 30, 100, 20, json); // Item Selector
		GuiButtonJson entityButton = new GuiButtonJson(2, this.width/2, this.height/2 - 30, 100, 20, json); // Entity Selector
		GuiButtonJson editButton = new GuiButtonJson(3, this.width/2 - 100, this.height/2 + 10, 200, 20, json); // JSON Editor
		
		itemButton.displayString = "Item";
		entityButton.displayString = "Entity";
		editButton.displayString = "Edit Raw NBT";
		
		this.buttonList.add(itemButton);
		this.buttonList.add(entityButton);
		this.buttonList.add(editButton);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		if(button.id == 1)
		{
			this.lastType = EditType.ITEM;
			json.entrySet().clear();
			NBTConverter.NBTtoJSON_Compound(stack.writeToNBT(new NBTTagCompound()), json);
			
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
		ENTITY
	}
}
