package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.resources.colors.GuiColorPulse;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.BoxLine;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.ColorTexture;
import betterquesting.client.gui2.CanvasQuestLine;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ToolboxToolGrab implements IToolboxTool
{
	private CanvasQuestLine gui;
	
	private boolean isGrabbing;
	private GuiRectangle selBounds;
	private final List<GrabEntry> grabList = new ArrayList<>();
	private final List<IGuiPanel> highlights = new ArrayList<>();
	
	private IGuiLine selLine = new BoxLine();
	private IGuiColor selCol = new GuiColorPulse(0xFFFFFFFF, 0xFF000000, 2F, 0F);
	
	@Override
	public void initTool(CanvasQuestLine gui)
	{
		this.gui = gui;
		
		isGrabbing = false;
		selBounds = null;
		grabList.clear();
	}
	
	@Override
	public void disableTool()
	{
	    for(GrabEntry grab : grabList)
        {
			IQuestLineEntry qle = gui.getQuestLine().getValue(grab.btn.getStoredValue().getID());
			
			if(qle != null)
            {
                grab.btn.rect.x = qle.getPosX();
                grab.btn.rect.y = qle.getPosY();
            }
        }
        
		isGrabbing = false;
		selBounds = null;
		grabList.clear();
		highlights.clear();
	}
	
	@Override
    public void refresh(CanvasQuestLine gui)
    {
        List<GrabEntry> tmp = new ArrayList<>();
        
        for(GrabEntry grab : grabList)
        {
            for(PanelButtonQuest btn : gui.getQuestButtons())
            {
                if(btn.getStoredValue().getID() == grab.btn.getStoredValue().getID())
                {
                    tmp.add(new GrabEntry(btn, grab.offX, grab.offY));
                    break;
                }
            }
        }
        
        grabList.clear();
        grabList.addAll(tmp);
        highlights.clear();
        
        if(grabList.size() <= 0)
        {
            isGrabbing = false;
            highlights.clear();
        } else if(!isGrabbing)
        {
            IGuiColor hCol = new GuiColorPulse(0x22FFFFFF, 0x77FFFFFF, 2F, 0F);
            for(GrabEntry grab : grabList) highlights.add(new PanelGeneric(grab.btn.rect, new ColorTexture(hCol)));
        }
    }
	
	@Override
	public void drawCanvas(int mx, int my, float partialTick)
	{
	    if(selBounds != null)
        {
            selBounds.w = mx - selBounds.x;
            selBounds.h = my - selBounds.y;
            
            selLine.drawLine(selBounds, selBounds, 2, selCol, partialTick);
        }
        
	    if(!isGrabbing)
        {
            // Draw highlights
            for(IGuiPanel pn : highlights) pn.drawPanel(mx, my, partialTick);
            return;
        }
	    
	    int snap = Math.max(1, ToolboxTabMain.INSTANCE.getSnapValue());
	    int dx = mx;
	    int dy = my;
	    dx = ((dx%snap) + snap)%snap;
	    dy = ((dy%snap) + snap)%snap;
	    dx = mx - dx;
	    dy = my - dy;
	    
	    for(GrabEntry grab : grabList)
        {
            grab.btn.rect.x = dx + grab.offX;
            grab.btn.rect.y = dy + grab.offY;
        }
	}
	
	@Override
    public void drawOverlay(int mx, int my, float partialTick)
    {
        ToolboxTabMain.INSTANCE.drawGrid(gui);
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        return !isGrabbing && selBounds == null ? null : Collections.emptyList();
    }
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click == 1) // Reset tool
		{
			for(GrabEntry grab : grabList)
            {
                IQuestLineEntry qle = gui.getQuestLine().getValue(grab.btn.getStoredValue().getID());
                
                if(qle != null)
                {
                    grab.btn.rect.x = qle.getPosX();
                    grab.btn.rect.y = qle.getPosY();
                }
            }
            
            isGrabbing = false;
            selBounds = null;
            grabList.clear();
            highlights.clear();
			return true;
		} else if(click != 0 || !gui.getTransform().contains(mx, my)) // Not a click we're listening for
		{
			return false;
		}
		
		if(isGrabbing) // Apply positioning
        {
            IQuestLine qLine = gui.getQuestLine();
			int lID = QuestLineDatabase.INSTANCE.getID(qLine);
            for(GrabEntry grab : grabList)
            {
			    IQuestLineEntry qle = gui.getQuestLine().getValue(grab.btn.getStoredValue().getID());
			    if(qle != null) qle.setPosition(grab.btn.rect.x, grab.btn.rect.y);
            }
            
            // Sync Line
            NBTTagCompound tag2 = new NBTTagCompound();
            NBTTagCompound base2 = new NBTTagCompound();
            base2.setTag("line", qLine.writeToNBT(new NBTTagCompound(), null));
            tag2.setTag("data", base2);
            tag2.setInteger("action", EnumPacketAction.EDIT.ordinal());
            tag2.setInteger("lineID", lID);
            PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tag2));
            
            isGrabbing = false;
            selBounds = null;
            
            if(grabList.size() <= 1) // Keep multi-selects active
            {
                grabList.clear();
                highlights.clear();
            }
            return true;
        }
        
        PanelButtonQuest btnClicked = gui.getButtonAt(mx, my);
		
		if(btnClicked != null) // Pickup the group or the single one if none are selected
        {
            if(grabList.size() > 0)
            {
                boolean canGrab = false;
                for(GrabEntry grab : grabList)
                {
                    if(grab.btn == btnClicked) canGrab = true;
                    grab.offX = grab.btn.rect.x - btnClicked.rect.x;
                    grab.offY = grab.btn.rect.y - btnClicked.rect.y;
                }
                
                if(!canGrab) return true; // The clicked button isn't part of the selection
            } else
            {
                grabList.add(new GrabEntry(btnClicked, 0, 0));
            }
            
            isGrabbing = true;
            return true;
        } else // Selection start
        {
            float zs = gui.getZoom();
            int lsx = gui.getScrollX();
            int lsy = gui.getScrollY();
            int tx = gui.getTransform().getX();
            int ty = gui.getTransform().getY();
            int smx = (int)((mx - tx) / zs) + lsx;
            int smy = (int)((my - ty) / zs) + lsy;
            
            selBounds = new GuiRectangle(smx, smy, 0, 0);
            return true;
        }
	}
	
	@Override
    public boolean onMouseRelease(int mx, int my, int click)
    {
        if(selBounds != null)
        {
            if(selBounds.w < 0)
            {
                selBounds.x += selBounds.w;
                selBounds.w *= -1;
            }
            
            if(selBounds.h < 0)
            {
                selBounds.y += selBounds.h;
                selBounds.h *= -1;
            }
            
            IGuiColor hCol = new GuiColorPulse(0x22FFFFFF, 0x77FFFFFF, 2F, 0F);
            
            boolean append = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            
            if(!append)
            {
                grabList.clear();
                highlights.clear();
            }
            
            topLoop:
            for(PanelButtonQuest btn : gui.getQuestButtons())
            {
                if(selBounds.contains(btn.rect.x + btn.rect.w / 2, btn.rect.y + btn.rect.h / 2))
                {
                    if(append) for(GrabEntry grab : grabList) if(grab.btn == btn) continue topLoop;
                    grabList.add(new GrabEntry(btn, 0, 0));
                    highlights.add(new PanelGeneric(btn.rect, new ColorTexture(hCol)));
                }
            }
            
            selBounds = null;
            return true;
        }
        
        return false;
    }
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
	    return false;
	}
	
	@Override
	public boolean onKeyPressed(char c, int keyCode)
	{
	    return false;
	}
	
	@Override
	public boolean clampScrolling()
	{
		return !isGrabbing;
	}
	
	private class GrabEntry
    {
        private final PanelButtonQuest btn;
        private int offX;
        private int offY;
        
        private GrabEntry(PanelButtonQuest btn, int offX, int offY)
        {
            this.btn = btn;
            this.offX = offX;
            this.offY = offY;
        }
    }
}
