package betterquesting.client.gui.editors.json.scrolling;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.MathHelper;
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
import betterquesting.client.gui.editors.json.GuiJsonTypeMenu;
import betterquesting.client.gui.editors.json.TextCallbackJsonArray;
import betterquesting.client.gui.editors.json.TextCallbackJsonObject;
import betterquesting.client.gui.editors.json.callback.JsonEntityCallback;
import betterquesting.client.gui.editors.json.callback.JsonFluidCallback;
import betterquesting.client.gui.editors.json.callback.JsonItemCallback;

public class ScrollingJsonEntry extends GuiElement implements IScrollingEntry
{
	private final GuiScrollingJson host;
	private final NBTBase json;
	private final IJsonDoc jDoc;
	private String name = "";
	private String desc = "";
	private String key = "";
	private int idx = -1;
	
	private NBTBase je;
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
	
	public ScrollingJsonEntry(GuiScrollingJson host, NBTTagCompound json, String key, IJsonDoc jDoc, boolean allowEdit)
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
		
		this.je = json.getTag(key);
		this.allowEdit = allowEdit;
	}
	
	public ScrollingJsonEntry(GuiScrollingJson host, NBTTagList json, int index, IJsonDoc jDoc, boolean allowEdit)
	{
		this.mc = Minecraft.getMinecraft();
		this.host = host;
		this.json = json;
		this.idx = index;
		this.jDoc = jDoc;
		
		this.je = index < 0 || index >= json.tagCount()? null : json.get(index);
		this.allowEdit = allowEdit;
		
		this.name = je == null? "" : ("#" + index);
	}
	
	public void setupEntry(int px, int width)
	{
		btnList.clear();
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
		} else if(je.getId() == 9)
		{
			btnMain = new GuiButtonJson<NBTTagList>(0, margin, 0, ctrlSpace - n, 20, (NBTTagList)je, false);
			btnList.add(btnMain);
		} else if(je.getId() == 10)
		{
			NBTTagCompound jo = (NBTTagCompound)je;
			
			if(JsonHelper.isItem(jo) || JsonHelper.isFluid(jo) || JsonHelper.isEntity(jo))
			{
				n += 20;
				btnAdv = new GuiButtonThemed(1, px + width - n, 0, 20, 20, "...");
				btnList.add(btnAdv);
			}
			
			btnMain = new GuiButtonJson<NBTTagCompound>(0, margin, 0, ctrlSpace - n, 20, jo, false);
			btnList.add(btnMain);
		} else if(je instanceof NBTPrimitive)
		{
			NBTPrimitive jp = (NBTPrimitive)je;
			
			/*if(jp.isBoolean())
			{
				btnMain = new GuiButtonJson<JsonPrimitive>(0, margin, 0, ctrlSpace - n, 20, jp, false);
				btnList.add(btnMain);
			} else if(jp.isNumber())*/
			{
				GuiNumberField num = new GuiNumberField(mc.fontRendererObj, margin + 1, 0, ctrlSpace - n - 2, 18);
				num.setMaxStringLength(Integer.MAX_VALUE);
				num.setText("" + jp.getDouble());
				txtMain = num;
			}// else
		} else if(je.getId() == 8)
		{
			NBTTagString jp = (NBTTagString)je;
			GuiBigTextField txt = new GuiBigTextField(mc.fontRendererObj, margin + 1, 1, ctrlSpace - n - 2, 18);
			txt.setMaxStringLength(Integer.MAX_VALUE);
			txt.setText(jp.getString());
			
			if(json.getId() == 10)
			{
				txt.enableBigEdit(new TextCallbackJsonObject((NBTTagCompound)json, key));
			} else if(json.getId() == 9)
			{
				txt.enableBigEdit(new TextCallbackJsonArray((NBTTagList)json, idx));
			}
			
			txtMain = txt;
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
		
		int length = mc.fontRendererObj.getStringWidth(name);
		mc.fontRendererObj.drawString(name, margin - 8 - length, py + 6, getTextColor(), false);
	}
	
	@Override
	public void drawForeground(int mx, int my, int px, int py, int width)
	{
		if(mx >= px && mx < px + width/3 && my >= py && my < py + 20 && desc != null && desc.length() > 0)
		{
			List<String> tTip = new ArrayList<String>();
			tTip.add(desc);
			this.drawTooltip(tTip, mx, my, mc.fontRendererObj);
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
				btn.playPressSound(mc.getSoundHandler());
				onActionPerformed(btn);
			}
		}
	}
	
	public void onActionPerformed(GuiButtonThemed btn)
	{
		if(je != null && btn.id == 3) // Delete Entry
		{
			if(json.getId() == 10)
			{
				((NBTTagCompound)json).removeTag(key);
				host.getEntryList().remove(this);
				host.refresh();
				return;
			} else if(json.getId() == 9)
			{
				((NBTTagList)json).removeTag(idx);
				host.getEntryList().remove(this);
				host.refresh(); 
				return;
			}
		} if(btn.id == 2)
		{
			if(json.getId() == 9)
			{
				mc.displayGuiScreen(new GuiJsonAdd(mc.currentScreen, (NBTTagList)json, idx));
				return;
			} else if(json.getId() == 10)
			{
				mc.displayGuiScreen(new GuiJsonAdd(mc.currentScreen, (NBTTagCompound)json));
				return;
			}
		} else if(je != null && je.getId() == 10)
		{
			NBTTagCompound jo = (NBTTagCompound)je;
			
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
				mc.displayGuiScreen(new GuiJsonTypeMenu(mc.currentScreen, jo));
			}
		} else if(je != null && je.getId() == 9)
		{
			mc.displayGuiScreen(new GuiJsonEditor(mc.currentScreen, (NBTTagList)je, jDoc));
		}/* else if(je != null && je.isJsonPrimitive() && je.getAsJsonPrimitive().isBoolean())
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
		}*/
	}
	
	public void onKeyTyped(char c, int keyCode)
	{
		if(txtMain != null)
		{
			txtMain.textboxKeyTyped(c, keyCode);
			
			if(json.getId() == 9)
			{
				//ArrayList<JsonElement> list = JsonHelper.GetUnderlyingArray(json.getAsJsonArray());
				NBTTagList list = (NBTTagList)json;
				
				if(txtMain instanceof GuiNumberField)
				{
					list.set(idx, new NBTTagDouble(((GuiNumberField)txtMain).getNumber().doubleValue()));
				} else
				{
					list.set(idx, new NBTTagString(txtMain.getText()));
				}
			} else if(json.getId() == 10)
			{
				NBTTagCompound tag = (NBTTagCompound)json;
				
				if(txtMain instanceof GuiNumberField)
				{
					tag.setDouble(key, ((GuiNumberField)txtMain).getNumber().doubleValue());
				} else
				{
					tag.setString(key, txtMain.getText());
				}
			}
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
