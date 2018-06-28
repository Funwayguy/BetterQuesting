package betterquesting.client.gui2.editors.nbt;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterDouble;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.client.gui.editors.json.*;
import betterquesting.client.gui.editors.json.callback.JsonEntityCallback;
import betterquesting.client.gui.editors.json.callback.JsonFluidCallback;
import betterquesting.client.gui.editors.json.callback.JsonItemCallback;
import betterquesting.client.gui.editors.json.scrolling.GuiJsonEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import org.lwjgl.input.Keyboard;

import java.util.Iterator;

// Self contained editing panel
public class PanelScrollingNBT extends CanvasScrolling implements IPEventListener
{
    private NBTBase nbt;
    
	private final int btnEdit;
	private final int btnAdv;
	private final int btnInsert;
	private final int btnDelete;
 
	public PanelScrollingNBT(IGuiRect rect, NBTTagCompound tag, int btnEdit, int btnAdv, int btnInsert, int btnDelete)
    {
        this(rect, btnEdit, btnAdv, btnInsert, btnDelete);
        
        this.setNBT(tag);
    }
    
	public PanelScrollingNBT(IGuiRect rect, NBTTagList tag, int btnEdit, int btnAdv, int btnInsert, int btnDelete)
    {
        this(rect, btnEdit, btnAdv, btnInsert, btnDelete);
        
        this.setNBT(tag);
    }
	
    private PanelScrollingNBT(IGuiRect rect, int btnEdit, int btnAdv, int btnInsert, int btnDelete)
    {
        super(rect);
        
        this.btnEdit = btnEdit;
        this.btnAdv = btnAdv;
        this.btnInsert = btnInsert;
        this.btnDelete = btnDelete;
        
        // Listens to its own buttons to update NBT values. The parent screen defines what the IDs are and any furter actions
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
		Keyboard.enableRepeatEvents(true);
    }
    
    public PanelScrollingNBT setNBT(NBTTagCompound tag)
    {
        this.nbt = tag;
        refreshList();
        return this;
    }
    
    public PanelScrollingNBT setNBT(NBTTagList list)
    {
        this.nbt = list;
        refreshList();
        return this;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        this.refreshList();
    }
    
    private void refreshList()
    {
        this.getAllPanels().clear();
        
        if(this.nbt == null)
        {
            return;
        }
        
        int preSX = getScrollX();
        int preSY = getScrollY();
        int width = getTransform().getWidth();
        int rw = (int)Math.ceil(width / 2F); // Width on right side (rounds up to account for rounding errors lost on left side)
        
        if(nbt.getId() == 10) // NBTTagCompound
        {
            NBTTagCompound tag = (NBTTagCompound)nbt;
            Iterator<String> keys = tag.getKeySet().iterator();
            int i = 0;
            
            while(keys.hasNext())
            {
                String k = keys.next();
                NBTBase entry = tag.getTag(k);
                
                PanelTextBox name = new PanelTextBox(new GuiRectangle(0, i * 16 + 4, width / 2 - 8, 16, 0), k).setAlignment(2);
                this.addPanel(name);
                
                if(entry.getId() == 10) // Object
                {
                    PanelButtonStorage<String> btn = new PanelButtonStorage<>(new GuiRectangle(width / 2, i * 16, rw - 48, 16, 0), btnEdit, "Object...", k);
                    this.addPanel(btn);
                    
                    btn = new PanelButtonStorage<>(new GuiRectangle(width - 48, i * 16, 16, 16, 0), btnAdv, "...", k);
                    this.addPanel(btn);
                } else if(entry.getId() == 9) // List
                {
                    PanelButtonStorage<String> btn = new PanelButtonStorage<>(new GuiRectangle(width / 2, i * 16, rw - 32, 16, 0), btnEdit, "List...", k);
                    this.addPanel(btn);
                } else if(entry.getId() == 1) // Byte/Boolean
                {
                
                } else if(entry.getId() > 1 && entry.getId() < 7) // Number
                {
                    // TODO: Handle each number type filter individually
                    PanelTextField<Double> text = new PanelTextField<>(new GuiRectangle(width / 2, i * 16, rw - 32, 16, 0), "" + ((NBTPrimitive)entry).getDouble(), FieldFilterDouble.INSTANCE);
                    this.addPanel(text);
                }
                
                PanelButtonStorage<String> btnI = new PanelButtonStorage<>(new GuiRectangle(width - 32, i * 16, 16, 16, 0), btnInsert, "+", k);
                btnI.setTextHighlight(new GuiColorStatic(128, 128, 128, 255), new GuiColorStatic(0, 255, 0, 255), new GuiColorStatic(0, 255, 0, 255));
                this.addPanel(btnI);
                
                PanelButtonStorage<String> btnD = new PanelButtonStorage<>(new GuiRectangle(width - 16, i * 16, 16, 16, 0), btnDelete, "x", k);
                btnD.setTextHighlight(new GuiColorStatic(128, 128, 128, 255), new GuiColorStatic(255, 0, 0, 255), new GuiColorStatic(255, 0, 0, 255));
                this.addPanel(btnD);
                
                i++;
            }
        } else if(nbt.getId() == 9) // NBTTagList
        {
            NBTTagList list = (NBTTagList)nbt;
            
            for(int i = 0; i < list.tagCount(); i++)
            {
                NBTBase entry = list.get(i);
                
                PanelTextBox name = new PanelTextBox(new GuiRectangle(0, i * 16 + 4, width / 2 - 8, 16, 0), "#" + i);
                this.addPanel(name);
                
                if(entry.getId() == 10) // Object
                {
                    PanelButtonStorage<Integer> btn = new PanelButtonStorage<>(new GuiRectangle(width / 2, i * 16, rw - 16, 16, 0), btnEdit, "Object...", i);
                    this.addPanel(btn);
                    
                    btn = new PanelButtonStorage<>(new GuiRectangle(width - 16, i * 16, 16, 16, 0), btnAdv, "...", i);
                    this.addPanel(btn);
                } else if(entry.getId() == 9) // List
                {
                    PanelButtonStorage<Integer> btn = new PanelButtonStorage<>(new GuiRectangle(width / 2, i * 16, rw, 16, 0), btnEdit, "List...", i);
                    this.addPanel(btn);
                } else if(entry.getId() == 1) // Byte/Boolean
                {
                
                } else if(entry.getId() > 1 && entry.getId() < 7) // Number
                {
                    // TODO: Handle each number type filter individually
                    PanelTextField<Double> text = new PanelTextField<>(new GuiRectangle(width / 2, i * 16, rw - 32, 16, 0), "" + ((NBTPrimitive)entry).getDouble(), FieldFilterDouble.INSTANCE);
                    this.addPanel(text);
                }
                
                PanelButtonStorage<Integer> btnI = new PanelButtonStorage<>(new GuiRectangle(width - 32, i * 16, 16, 16, 0), btnInsert, "+", i);
                btnI.setTextHighlight(new GuiColorStatic(128, 128, 128, 255), new GuiColorStatic(0, 255, 0, 255), new GuiColorStatic(0, 255, 0, 255));
                this.addPanel(btnI);
                
                PanelButtonStorage<Integer> btnD = new PanelButtonStorage<>(new GuiRectangle(width - 16, i * 16, 16, 16, 0), btnDelete, "x", i);
                btnD.setTextHighlight(new GuiColorStatic(128, 128, 128, 255), new GuiColorStatic(255, 0, 0, 255), new GuiColorStatic(255, 0, 0, 255));
                this.addPanel(btnD);
            }
        }
        
        this.setScrollX(preSX);
        this.setScrollY(preSY);
    }
    
    @Override
    public void onPanelEvent(PanelEvent event)
    {
        if(event instanceof PEventButton)
        {
            onButtonPress((PEventButton)event);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void onButtonPress(PEventButton event)
    {
        if(nbt == null)
        {
            return;
        }
        
        IPanelButton btn = event.getButton();
        Minecraft mc = Minecraft.getMinecraft();
        NBTBase entry;
        
        if(!(btn.getButtonID() == btnEdit || btn.getButtonID() == btnAdv || btn.getButtonID() == btnInsert || btn.getButtonID() == btnDelete))
        {
            return;
        }
        
        if(nbt.getId() == 10)
        {
            entry = ((NBTTagCompound)nbt).getCompoundTag(((PanelButtonStorage<String>)btn).getStoredValue());
        } else if(nbt.getId() == 9)
        {
            entry = ((NBTTagList)nbt).get(((PanelButtonStorage<Integer>)btn).getStoredValue());
        } else
        {
            entry = new NBTTagEnd(); // Fallback that should NEVER have to be used
        }
        
        if(btn.getButtonID() == btnEdit) // Context dependent action/toggle
        {
            if(entry.getId() == 10) // Object editor
            {
                NBTTagCompound tag = (NBTTagCompound)entry;
                
                if(JsonHelper.isItem(tag))
                {
                    mc.displayGuiScreen(new GuiJsonItemSelection(mc.currentScreen, new JsonItemCallback(tag), JsonHelper.JsonToItemStack(tag)));
                } else if(JsonHelper.isFluid(tag))
                {
                    mc.displayGuiScreen(new GuiJsonFluidSelection(mc.currentScreen, new JsonFluidCallback(tag), JsonHelper.JsonToFluidStack(tag)));
                } else if(JsonHelper.isEntity(tag))
                {
                    mc.displayGuiScreen(new GuiJsonEntitySelection(mc.currentScreen, new JsonEntityCallback(tag), JsonHelper.JsonToEntity(tag, mc.world)));
                } else
                {
					//mc.displayGuiScreen(new GuiJsonEditor(mc.currentScreen, tag, null));
                    mc.displayGuiScreen(new GuiNbtEditor(mc.currentScreen, tag, null));
                }
            } else if(entry.getId() == 9) // List editor
            {
                mc.displayGuiScreen(new GuiJsonEditor(mc.currentScreen, (NBTTagList)entry, null));
            } else if(entry.getId() == 8) // Text editor
            {
            
            } else if(entry.getId() == 1) // Byte/Boolean toggle
            {
            
            } else if(entry.getId() == 7 || entry.getId() == 11 || entry.getId() == 12) // Byte/Integer/Long array
            {
                // TODO: Add supportted editors for Byte, Integer and Long Arrays
                throw new UnsupportedOperationException("NBTTagByteArray, NBTTagIntArray and NBTTagLongArray are not currently supported yet");
            }
        } else if(btn.getButtonID() == btnAdv) // Open advanced editor (on supported types)
        {
            if(entry.getId() == 10)
            {
                // TODO: Replace
                mc.displayGuiScreen(new GuiJsonTypeMenu(mc.currentScreen, (NBTTagCompound)entry));
            } else if(entry.getId() == 9) // Not currently available but will be when context list editors (enchantments/inventories/etc) are available
            {
                // TODO: Replace
                mc.displayGuiScreen(new GuiJsonEditor(mc.currentScreen, (NBTTagList)entry, null));
            }
        } else if(btn.getButtonID() == btnInsert)
        {
            if(nbt.getId() == 10)
            {
                mc.displayGuiScreen(new GuiJsonAdd(mc.currentScreen, (NBTTagCompound)nbt));
            } else if(nbt.getId() == 9)
            {
                mc.displayGuiScreen(new GuiJsonAdd(mc.currentScreen, (NBTTagList)nbt, ((PanelButtonStorage<Integer>)btn).getStoredValue()));
            }
        } else if(btn.getButtonID() == btnDelete)
        {
            if(nbt.getId() == 10)
            {
                ((NBTTagCompound)nbt).removeTag(((PanelButtonStorage<String>)btn).getStoredValue());
                refreshList();
            } else if(nbt.getId() == 9)
            {
                ((NBTTagList)nbt).removeTag(((PanelButtonStorage<Integer>)btn).getStoredValue());
                refreshList();
            }
        }
    }
}
