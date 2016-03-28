package betterquesting.client.gui.editors.json;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiBigTextField;
import betterquesting.client.gui.misc.GuiButtonJson;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiNumberField;
import betterquesting.client.gui.misc.ITextEditor;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiJsonObject extends GuiQuesting implements ITextEditor, IVolatileScreen
{
	HashMap<Integer,String> idMap = new HashMap<Integer,String>();
	int scrollPos = 0;
	JsonObject settings;
	boolean allowEdit = true;
	
	/**
	 * List of GuiTextFields and GuiButtons
	 */
	HashMap<String, JsonControlSet> editables = new HashMap<String, JsonControlSet>();
	
	public GuiJsonObject(GuiScreen parent, JsonObject settings)
	{
		super(parent, "betterquesting.title.json_object");
		this.settings = settings;
	}
	
	/**
	 * Set whether or not a user can add/remove entries from this JsonElement
	 * @param state
	 * @return
	 */
	public GuiJsonObject SetEditMode(boolean state)
	{
		this.allowEdit = state;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		idMap = new HashMap<Integer,String>();
		editables = new HashMap<String, JsonControlSet>();
		int maxRows = (this.sizeY - 84)/20;
		
		((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
		((GuiButton)this.buttonList.get(0)).width = 100;
		this.buttonList.add(new GuiButtonQuesting(1, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, I18n.format("betterquesting.btn.new")));
		this.buttonList.add(new GuiButtonQuesting(2, this.width/2, this.guiTop + 32 + (maxRows * 20), 20, 20, "<"));
		this.buttonList.add(new GuiButtonQuesting(3, this.guiLeft + this.sizeX - 36, this.guiTop + 32 + (maxRows * 20), 20, 20, ">"));
		
        Keyboard.enableRepeatEvents(true);
        
        int i = -1;
		for(Entry<String,JsonElement> entry : settings.entrySet())
		{
			i++;
			idMap.put(i, entry.getKey());
			if(entry.getValue().isJsonPrimitive())
			{
				JsonPrimitive jPrim = entry.getValue().getAsJsonPrimitive();
				GuiTextField txtBox;
				if(jPrim.isNumber())
				{
					txtBox = new GuiNumberField(this.fontRendererObj, 32, -9999, 128, 16);
					txtBox.setText("" + jPrim.getAsNumber());
				} else if(jPrim.isBoolean())
				{
					GuiButtonJson button = new GuiButtonJson(buttonList.size(), -9999, -9999, 128, 20, jPrim);
					this.buttonList.add(button);
					editables.put(entry.getKey(), new JsonControlSet(this.buttonList, button, false, allowEdit));
					continue;
				} else
				{
					txtBox = new GuiBigTextField(this.fontRendererObj, 32, -9999, 128, 16).enableBigEdit(this, i);
					txtBox.setMaxStringLength(Integer.MAX_VALUE);
					txtBox.setText(jPrim.getAsString());
				}

				editables.put(entry.getKey(), new JsonControlSet(this.buttonList, txtBox, false, allowEdit));
			} else
			{
				GuiButtonJson button = new GuiButtonJson(buttonList.size(), -9999, -9999, 128, 20, entry.getValue());
				this.buttonList.add(button);
				editables.put(entry.getKey(), new JsonControlSet(this.buttonList, button, false, allowEdit));
			}
		}
	}
	
	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
		// >> Send new settings to the server here <<
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
        int mx = Mouse.getEventX() * mc.currentScreen.width / mc.displayWidth;
        int my = mc.currentScreen.height - Mouse.getEventY() * mc.currentScreen.height / mc.displayHeight - 1;
        
		if(button.id == 0)
		{
			this.mc.displayGuiScreen(parent);
		} else if(button.id == 1)
		{
			this.mc.displayGuiScreen(new GuiJsonAdd(this, this.settings));
		} else if(button.id == 2)
		{
			if(scrollPos > 0)
			{
				scrollPos -= 1;
			} else
			{
				scrollPos = 0;
			}
		} else if(button.id == 3)
		{
			int maxShow = (this.sizeY - 84)/20;
			if((scrollPos + 1) * maxShow < editables.size())
			{
				scrollPos += 1;
			}
		} else
		{
			for(String key : editables.keySet())
			{
				JsonControlSet controls = editables.get(key);
				
				if(controls == null)
				{
					continue;
				}
				
				if(button == controls.removeButton)
				{
					settings.remove(key);
					this.buttonList.remove(controls.jsonDisplay);
					this.buttonList.remove(controls.addButton);
					this.buttonList.remove(controls.removeButton);
					editables.remove(key);
					break;
				} else if(button == controls.jsonDisplay && button instanceof GuiButtonJson)
				{
					GuiButtonJson jsonButton = (GuiButtonJson)button;
					JsonElement element = jsonButton.json;
					
					GuiScreen jGui = jsonButton.getJsonScreen(this, mx, my, allowEdit);
					
					if(jGui != null)
					{
						this.mc.displayGuiScreen(jGui);
					} else if(jsonButton.isItemStack() || jsonButton.isEntity() || jsonButton.isFluid())
					{
						this.mc.displayGuiScreen(new GuiJsonTypeMenu(this, element.getAsJsonObject()));
					} else if(element.isJsonObject())
					{
						this.mc.displayGuiScreen(new GuiJsonObject(this, element.getAsJsonObject()).SetEditMode(this.allowEdit));
					} else if(element.isJsonArray())
					{
						this.mc.displayGuiScreen(new GuiJsonArray(this, element.getAsJsonArray()).SetEditMode(this.allowEdit));
					} else if(element.isJsonPrimitive())
					{
						if(element.getAsJsonPrimitive().isBoolean())
						{
							JsonPrimitive jBool = new JsonPrimitive(!element.getAsBoolean());
							settings.add(key, jBool);
							jsonButton.displayString = "" + jBool.getAsBoolean();
							jsonButton.json = jBool;
						}
					}
					break;
				}
			}
		}
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		int maxRows = (this.sizeY - 84)/20;
		
		String[] keys = editables.keySet().toArray(new String[]{});
		
		for(int i = 0; i < keys.length; i++)
		{
			JsonControlSet controls = editables.get(keys[i]);
			
			if(controls == null)
			{
				continue;
			}
			
			int n = i - (scrollPos * maxRows);
			
			int posX = this.guiLeft + (sizeX/2);
			int posY = -9999;
			
			if(n >= 0 && n < maxRows)
			{
				posY = this.guiTop + 32 + (n * 20);
				controls.drawControls(this, posX, posY, sizeX/2 - 16, 20, mx, my, partialTick);
			} else
			{
				controls.Disable();
			}
			
			this.fontRendererObj.drawString(keys[i], this.guiLeft + (sizeX/2) - this.fontRendererObj.getStringWidth(keys[i]) - 8, posY + 4, ThemeRegistry.curTheme().textColor().getRGB(), false);
		}
		
		int mxPage = Math.max(MathHelper.ceiling_float_int(editables.size()/(float)maxRows), 1);
		String txt = (scrollPos + 1) + "/" + mxPage;
		this.fontRendererObj.drawString(txt, guiLeft + 16 + (sizeX - 32)/4*3 - this.fontRendererObj.getStringWidth(txt)/2, guiTop + 32 + (maxRows * 20) + 6, ThemeRegistry.curTheme().textColor().getRGB());
	}
	
	@Override
	public void mouseClicked(int x, int y, int type)
	{
		super.mouseClicked(x, y, type);
		
		for(JsonControlSet controls : editables.values())
		{
			controls.mouseClick(this, x, y, type);
		}
	}
	
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int num)
    {
		super.keyTyped(character, num);
		
		for(Entry<String, JsonControlSet> entry : editables.entrySet())
		{
			if(entry.getValue().jsonDisplay instanceof GuiTextField)
			{
				GuiTextField textField = (GuiTextField)entry.getValue().jsonDisplay;
				textField.textboxKeyTyped(character, num);
				
				if(settings.getAsJsonPrimitive(entry.getKey()).isNumber())
				{
					if(textField instanceof GuiNumberField)
					{
						settings.add(entry.getKey(), new JsonPrimitive(((GuiNumberField)textField).getNumber()));
					} else
					{
						try
						{
							settings.add(entry.getKey(), new JsonPrimitive(NumberFormat.getInstance().parse(textField.getText())));
						} catch(Exception e)
						{
							BetterQuesting.logger.log(Level.ERROR, "Unable to parse number format for JsonObject!", e);
							settings.add(entry.getKey(), new JsonPrimitive(textField.getText()));
						}
					}
				} else
				{
					settings.add(entry.getKey(), new JsonPrimitive(textField.getText()));
				}
			}
		}
    }

	@Override
	public void setText(int id, String text)
	{
		if(settings == null || !idMap.containsKey(id))
		{
			return;
		}
		
		String key = idMap.get(id);
		
		settings.addProperty(key, text);
	}
}
