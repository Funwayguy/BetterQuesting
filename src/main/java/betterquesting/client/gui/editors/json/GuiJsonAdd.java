package betterquesting.client.gui.editors.json;

import java.awt.Color;
import java.util.ArrayList;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.JsonIO;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@SideOnly(Side.CLIENT)
public class GuiJsonAdd extends GuiQuesting
{
	GuiTextField keyText;
	JsonElement json;
	int select = 0;
	int insertIdx = 0;
	
	public GuiJsonAdd(GuiScreen parent, JsonArray json, int insertIdx) // JsonArray
	{
		super(parent, "Add JSON Element");
		this.json = json;
		this.insertIdx = insertIdx;
	}
	
	public GuiJsonAdd(GuiScreen parent, JsonObject json) // JsonObject
	{
		super(parent, "Add JSON Element");
		this.json = json;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		((GuiButton)this.buttonList.get(0)).xPosition = this.guiLeft + this.sizeX/2 - 100;
		((GuiButton)this.buttonList.get(0)).width = 100;
		this.buttonList.add(new GuiButtonQuesting(1, this.guiLeft + this.sizeX/2, this.guiTop + this.sizeY - 16, 100, 20, "Cancel"));
		
		if(json.isJsonObject())
		{
			keyText = new GuiTextField(this.fontRendererObj, this.guiLeft + this.sizeX/2 - 100, this.guiTop + this.sizeY/2 - 48, 200, 16);
			keyText.setMaxStringLength(Integer.MAX_VALUE);
			((GuiButton)this.buttonList.get(0)).enabled = false;
		}
		
		GuiButtonQuesting buttonStr = new GuiButtonQuesting(2, this.guiLeft + this.sizeX/2 - 100, this.guiTop + this.sizeY/2 - 20, 200, 20, "Text");
		GuiButtonQuesting buttonNum = new GuiButtonQuesting(3, this.guiLeft + this.sizeX/2 - 100, this.guiTop + this.sizeY/2 - 00, 200, 20, "Number");
		GuiButtonQuesting buttonObj = new GuiButtonQuesting(4, this.guiLeft + this.sizeX/2 - 100, this.guiTop + this.sizeY/2 + 20, 200, 20, "Json Object");
		GuiButtonQuesting buttonArr = new GuiButtonQuesting(5, this.guiLeft + this.sizeX/2 - 100, this.guiTop + this.sizeY/2 + 40, 200, 20, "Json Array");
		GuiButtonQuesting buttonItm = new GuiButtonQuesting(6, this.guiLeft + this.sizeX/2 - 100, this.guiTop + this.sizeY/2 + 60, 200, 20, "Item");
		
		buttonStr.enabled = false; // Default selection, init disabled
		
		this.buttonList.add(buttonStr);
		this.buttonList.add(buttonNum);
		this.buttonList.add(buttonObj);
		this.buttonList.add(buttonArr);
		this.buttonList.add(buttonItm);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(keyText != null)
		{
			keyText.drawTextBox();
		}
		
		if(keyText != null)
		{
			this.drawString(this.fontRendererObj, "Key", this.guiLeft + (sizeX/2) - this.fontRendererObj.getStringWidth("Key")/2, this.guiTop + 52, ThemeRegistry.curTheme().textColor().getRGB());
			
			if(keyText.getText().length() <= 0)
			{
				this.drawString(this.fontRendererObj, "No key entered!", this.guiLeft + (sizeX/2) - this.fontRendererObj.getStringWidth("No key entered!")/2, this.guiTop + this.sizeY/2 - 30, Color.RED.getRGB());
			} else if(json.getAsJsonObject().has(keyText.getText()))
			{
				this.drawString(this.fontRendererObj, "Key already exists!", this.guiLeft + (sizeX/2) - this.fontRendererObj.getStringWidth("Key already exists!")/2, this.guiTop + this.sizeY/2 - 30, Color.RED.getRGB());
			}
		}
	}
	
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int num)
    {
		super.keyTyped(character, num);
		
		if(keyText != null)
		{
			keyText.textboxKeyTyped(character, num);
			
			if(keyText.getText().length() <= 0 || json.getAsJsonObject().has(keyText.getText()))
			{
				keyText.setTextColor(Color.RED.getRGB());
				((GuiButton)this.buttonList.get(0)).enabled = false;
			} else
			{
				keyText.setTextColor(Color.WHITE.getRGB());
				((GuiButton)this.buttonList.get(0)).enabled = true;
			}
		} else
		{
			((GuiButton)this.buttonList.get(0)).enabled = true;
		}
    }
	
	@Override
	public void mouseClicked(int x, int y, int type)
	{
		super.mouseClicked(x, y, type);
		if(keyText != null)
		{
			this.keyText.mouseClicked(x, y, type);
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		if(button.id == 0 || button.id == 1)
		{
			if(button.id == 0)
			{
				JsonElement newObj = null;
				switch(select)
				{
					case 0:
					{
						newObj = new JsonPrimitive("");
						break;
					}
					case 1:
					{
						newObj = new JsonPrimitive(0);
						break;
					}
					case 2:
					{
						newObj = new JsonObject();
						break;
					}
					case 3:
					{
						newObj = new JsonArray();
						break;
					}
					case 4:
					{
						newObj = NBTConverter.NBTtoJSON_Compound(new ItemStack(Blocks.stone).writeToNBT(new NBTTagCompound()), new JsonObject());
						break;
					}
				}
				
				if(newObj != null)
				{
					if(json.isJsonObject())
					{
						json.getAsJsonObject().add(keyText.getText(), newObj);
					} else if(json.isJsonArray())
					{
						// Insert function for array
						ArrayList<JsonElement> list = JsonIO.GetUnderlyingArray(json.getAsJsonArray());
						
						if(list != null)
						{
							list.add(insertIdx, newObj);
						} else
						{
							return; // Error!
						}
					}
				} else
				{
					return; // Error!
				}
			}
			
			this.mc.displayGuiScreen(parent);
		} else if(button.id <= 6)
		{
			((GuiButton)this.buttonList.get(this.select+2)).enabled = true;
			button.enabled = false;
			this.select = button.id - 2;
		}
	}
}
