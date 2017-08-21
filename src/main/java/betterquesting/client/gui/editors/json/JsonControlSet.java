package betterquesting.client.gui.editors.json;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.client.gui.controls.GuiButtonThemed;

@SideOnly(Side.CLIENT)
public class JsonControlSet
{
	/**
	 * The Gui object used to represent the JsonElement
	 */
	public Gui jsonDisplay;
	public GuiButton addButton;
	public GuiButton removeButton;
	
	/**
	 * New text field control set
	 */
	@SuppressWarnings("rawtypes")
	public JsonControlSet(List btnList, GuiTextField jsonGui, boolean canAdd, boolean canRemove)
	{
		this(btnList, (Gui)jsonGui, canAdd, canRemove);
	}
	
	/**
	 * New button control set
	 */
	@SuppressWarnings("rawtypes")
	public JsonControlSet(List btnList, GuiButton jsonGui, boolean canAdd, boolean canRemove)
	{
		this(btnList, (Gui)jsonGui, canAdd, canRemove);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private JsonControlSet(List btnList, Gui jsonGui, boolean canAdd, boolean canRemove)
	{
		this.jsonDisplay = jsonGui;
		
		if(canAdd)
		{
			addButton = new GuiButtonThemed(btnList.size(), -9999, -9999, 20, 20, "+", true);
			addButton.packedFGColour = Color.GREEN.getRGB();
			btnList.add(addButton);
		}
		
		if(canRemove)
		{
			removeButton = new GuiButtonThemed(btnList.size(), -9999, -9999, 20, 20, "x", true);
			removeButton.packedFGColour = Color.RED.getRGB();
			btnList.add(removeButton);
		}
	}
	
	public void Disable()
	{
		if(jsonDisplay instanceof GuiButton)
		{
			GuiButton button = (GuiButton)jsonDisplay;
			button.x = -9999;
			button.y = -9999;
		} else if(jsonDisplay instanceof GuiTextField)
		{
			GuiTextField textField = (GuiTextField)jsonDisplay;
			textField.x = -9999;
			textField.y = -9999;
		}
		
		if(addButton != null)
		{
			addButton.visible = false;
		}
		
		if(removeButton != null)
		{
			removeButton.visible = false;
		}
	}
	
	public void mouseClick(GuiScreen screen, int mx, int my, int type)
	{
		if(addButton != null)
		{
			addButton.mousePressed(screen.mc, mx, my);
		}
		
		if(removeButton != null)
		{
			removeButton.mousePressed(screen.mc, mx, my);
		}
		
		if(jsonDisplay instanceof GuiButton)
		{
			GuiButton button = (GuiButton)jsonDisplay;
			button.mousePressed(screen.mc, mx, my);
		} else if(jsonDisplay instanceof GuiTextField)
		{
			GuiTextField textField = (GuiTextField)jsonDisplay;
			textField.mouseClicked(mx, my, type);
		}
	}
	
	public void drawControls(GuiScreen screen, int posX, int posY, int sizeX, int sizeY, int mx, int my, float partialTick)
	{
		int ctrlSpace = 0;
		
		if(addButton != null)
		{
			ctrlSpace += 20;
			addButton.visible = true;
		}
		
		if(removeButton != null)
		{
			ctrlSpace += 20;
			removeButton.visible = true;
		}
		
		if(this.jsonDisplay instanceof GuiTextField)
		{
			GuiTextField textField = (GuiTextField)this.jsonDisplay;
			
			textField.x = posX + 1;
			textField.y = posY + 1;
			textField.width = sizeX - ctrlSpace - 2;
			textField.height = 18;
			textField.drawTextBox();
		} else if(this.jsonDisplay instanceof GuiButton)
		{
			GuiButton button = (GuiButton)this.jsonDisplay;
			
			button.x = posX;
			button.y = posY;
			button.width = sizeX - ctrlSpace;
		}
		
		if(addButton != null)
		{
			addButton.x = posX + sizeX - ctrlSpace;
			addButton.y = posY;
		}
		
		if(removeButton != null)
		{
			removeButton.x = posX + sizeX - ctrlSpace + (addButton == null? 0 : 20);
			removeButton.y = posY;
		}
	}
}
