package betterquesting.client.gui.editors.json.scrolling;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonJson;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.controls.GuiNumberField;
import betterquesting.api.client.gui.lists.IScrollingEntry;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.utils.JsonHelper;
import betterquesting.client.gui.editors.json.GuiJsonAdd;
import betterquesting.client.gui.editors.json.GuiJsonEntitySelection;
import betterquesting.client.gui.editors.json.GuiJsonFluidSelection;
import betterquesting.client.gui.editors.json.GuiJsonItemSelection;
import betterquesting.client.gui.editors.json.TextCallbackJsonArray;
import betterquesting.client.gui.editors.json.TextCallbackJsonObject;
import betterquesting.client.gui.editors.json.callback.JsonEntityCallback;
import betterquesting.client.gui.editors.json.callback.JsonFluidCallback;
import betterquesting.client.gui.editors.json.callback.JsonItemCallback;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ScrollingJsonEntry extends GuiElement implements IScrollingEntry
{
	private final GuiScrollingJson host;
	private final JsonElement json;
	private final IJsonDoc jDoc;
	private String name = "";
	private String desc = "";
	private String key = "";
	private int idx = -1;
	
	private JsonElement je;
	private boolean allowEdit = true;
	
	private GuiTextField txtMain;
	private GuiButtonThemed btnMain;
	
	private GuiButtonThemed btnAdv;
	private GuiButtonThemed btnAdd;
	private GuiButtonThemed btnDel;
	
	private final Minecraft mc;
	
	private List<GuiButtonThemed> btnList = new ArrayList<GuiButtonThemed>();
	
	/* Button ID Reference
	 * 0 - Main Action
	 * 1 - Sub Action
	 * 2 - Insert Above
	 * 3 - Delete Entry
	 */
	
	public ScrollingJsonEntry(GuiScrollingJson host, JsonObject json, String key, IJsonDoc jDoc, boolean allowEdit)
	{
		this.mc = Minecraft.getMinecraft();
		this.host = host;
		this.json = json;
		this.key = key;
		this.jDoc = jDoc;
		
		this.name = key;
		
		if(jDoc != null)
		{
			String uln = jDoc.getUnlocalisedName(name);
			String ln = I18n.format(uln);
			
			if(!ln.equalsIgnoreCase(uln))
			{
				this.name = ln;
				this.desc = jDoc == null? "" : I18n.format(jDoc.getUnlocalisedDesc(key));
			}
		}
		
		this.je = json.get(key);
		this.allowEdit = allowEdit;
	}
	
	public ScrollingJsonEntry(GuiScrollingJson host, JsonArray json, int index, IJsonDoc jDoc, boolean allowEdit)
	{
		this.mc = Minecraft.getMinecraft();
		this.host = host;
		this.json = json;
		this.idx = index;
		this.jDoc = jDoc;
		
		this.je = index < 0 || index >= json.size()? null : json.get(index);
		this.allowEdit = allowEdit;
		
		this.name = je == null? "" : ("#" + index);
	}
	
	public void setupEntry(int px, int width)
	{
		int margin = px + (width/3);
		int ctrlSpace = MathHelper.ceiling_float_int((width/3F)*2F);
		int n = 0;
		
		if(allowEdit && je != null)
		{
			n = 20;
			btnDel = new GuiButtonThemed(3, px + width - n, 0, 20, 20, "x");
			btnDel.packedFGColour = Color.RED.getRGB();
			btnList.add(btnDel);
		}
		
		if(allowEdit)
		{
			n = 40;
			btnAdd = new GuiButtonThemed(2, px + width - n, 0, 20, 20, "+");
			btnAdd.packedFGColour = Color.GREEN.getRGB();
			btnList.add(btnAdd);
		}
		
		if(je == null)
		{
			return;
		} else if(je.isJsonArray())
		{
			btnMain = new GuiButtonJson<JsonArray>(0, margin, 0, ctrlSpace - n, 20, je.getAsJsonArray(), false);
			btnList.add(btnMain);
		} else if(je.isJsonObject())
		{
			JsonObject jo = je.getAsJsonObject();
			
			if(JsonHelper.isItem(jo) || JsonHelper.isFluid(jo) || JsonHelper.isEntity(jo))
			{
				n += 20;
				btnAdv = new GuiButtonThemed(1, px + width - n, 0, 20, 20, "...");
				btnList.add(btnAdv);
			}
			
			btnMain = new GuiButtonJson<JsonObject>(0, margin, 0, ctrlSpace - n, 20, je.getAsJsonObject(), false);
			btnList.add(btnMain);
		} else if(je.isJsonPrimitive())
		{
			JsonPrimitive jp = je.getAsJsonPrimitive();
			
			if(jp.isBoolean())
			{
				btnMain = new GuiButtonJson<JsonPrimitive>(0, margin, 0, ctrlSpace - n, 20, jp, false);
				btnList.add(btnMain);
			} else if(jp.isNumber())
			{
				GuiNumberField num = new GuiNumberField(mc.fontRenderer, margin + 1, 0, ctrlSpace - n - 2, 18);
				num.setText(jp.getAsNumber().toString());
				txtMain = num;
			} else
			{
				GuiBigTextField txt = new GuiBigTextField(mc.fontRenderer, margin + 1, 1, ctrlSpace - n - 2, 18);
				txt.setText(jp.getAsString());
				
				if(json.isJsonObject())
				{
					txt.enableBigEdit(new TextCallbackJsonObject(json.getAsJsonObject(), key));
				} else if(json.isJsonArray())
				{
					txt.enableBigEdit(new TextCallbackJsonArray(json.getAsJsonArray(), idx));
				}
				
				txtMain = txt;
			}
		}
	}
	
	@Override
	public void drawBackground(int mx, int my, int px, int py, int width)
	{
		int margin = px + (width/3);
		
		for(GuiButtonThemed btn : btnList)
		{
			btn.yPosition = py;
			btn.drawButton(mc, mx, my);
		}
		
		if(txtMain != null)
		{
			txtMain.yPosition = py + 1;
			txtMain.drawTextBox();
		}
		
		int length = mc.fontRenderer.getStringWidth(name);
		mc.fontRenderer.drawString(name, margin - 8 - length, py + 6, getTextColor(), false);
	}
	
	@Override
	public void drawForeground(int mx, int my, int px, int py, int width)
	{
		if(mx >= px && mx < px + width/3 && my >= py && my < py + 20 && desc != null && desc.length() > 0)
		{
			List<String> tTip = new ArrayList<String>();
			tTip.add(desc);
			this.drawTooltip(tTip, mx, my, mc.fontRenderer);
		}
	}
	
	@Override
	public void onMouseClick(int mx, int my, int px, int py, int click, int index)
	{
		if(txtMain != null)
		{
			txtMain.mouseClicked(mx, my, click);
		}
		
		for(GuiButtonThemed btn : btnList)
		{
			if(btn.mousePressed(mc, mx, my))
			{
				btn.func_146113_a(mc.getSoundHandler());
				onActionPerformed(btn);
			}
		}
	}
	
	public void onActionPerformed(GuiButtonThemed btn)
	{
		if(je != null && btn.id == 3) // Delete Entry
		{
			if(json.isJsonObject())
			{
				json.getAsJsonObject().remove(key);
				host.getEntryList().remove(this);
				host.refresh();
				return;
			} else if(json.isJsonArray())
			{
				JsonHelper.GetUnderlyingArray(json.getAsJsonArray()).remove(idx);
				host.getEntryList().remove(this);
				host.refresh(); 
				return;
			}
		} if(btn.id == 2)
		{
			if(json.isJsonArray())
			{
				mc.displayGuiScreen(new GuiJsonAdd(mc.currentScreen, json.getAsJsonArray(), idx));
				return;
			} else if(json.isJsonObject())
			{
				mc.displayGuiScreen(new GuiJsonAdd(mc.currentScreen, json.getAsJsonObject()));
				return;
			}
		} else if(je != null && je.isJsonObject())
		{
			JsonObject jo = je.getAsJsonObject();
			
			IJsonDoc childDoc = jDoc == null? null : jDoc.getChildDoc(key);
			
			if(btn.id == 0) // Main
			{
				if(!(JsonHelper.isItem(jo) || JsonHelper.isFluid(jo) || JsonHelper.isEntity(jo)))
				{
					mc.displayGuiScreen(new GuiJsonEditor(mc.currentScreen, jo, childDoc));
				} else if(JsonHelper.isItem(jo))
				{
					mc.displayGuiScreen(new GuiJsonItemSelection(mc.currentScreen, new JsonItemCallback(jo), JsonHelper.JsonToItemStack(jo)));
				} else if(JsonHelper.isFluid(jo))
				{
					mc.displayGuiScreen(new GuiJsonFluidSelection(mc.currentScreen, new JsonFluidCallback(jo), JsonHelper.JsonToFluidStack(jo)));
				} else if(JsonHelper.isEntity(jo))
				{
					mc.displayGuiScreen(new GuiJsonEntitySelection(mc.currentScreen, new JsonEntityCallback(jo), JsonHelper.JsonToEntity(jo, mc.theWorld, true)));
				}
			} else if(btn.id == 1) // Advanced
			{
				mc.displayGuiScreen(new GuiJsonEditor(mc.currentScreen, jo, childDoc));
			}
		} else if(je != null && je.isJsonArray())
		{
			mc.displayGuiScreen(new GuiJsonEditor(mc.currentScreen, je.getAsJsonArray(), jDoc));
		} else if(je != null && je.isJsonPrimitive() && je.getAsJsonPrimitive().isBoolean())
		{
			if(json.isJsonObject())
			{
				json.getAsJsonObject().addProperty(key, !je.getAsBoolean());
				host.refresh();
				return;
			} else if(json.isJsonArray())
			{
				JsonHelper.GetUnderlyingArray(json.getAsJsonArray()).set(idx, new JsonPrimitive(!je.getAsBoolean()));
				host.refresh();
				return;
			}
		}
	}
	
	public void onKeyTyped(char c, int keyCode)
	{
		if(txtMain != null)
		{
			txtMain.textboxKeyTyped(c, keyCode);
		}
	}
	
	@Override
	public int getHeight()
	{
		return 20;
	}
	
	@Override
	public boolean canDrawOutsideBox(boolean isForeground)
	{
		return isForeground;
	}
}
