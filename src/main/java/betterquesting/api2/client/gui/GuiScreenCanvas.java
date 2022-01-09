package betterquesting.api2.client.gui;

import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.popups.PopChoice;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.gui2.GuiQuestSearch;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuiScreenCanvas extends GuiScreen implements IScene
{
	private final List<IGuiPanel> guiPanels = new CopyOnWriteArrayList<>();
	private final GuiRectangle rootTransform = new GuiRectangle(0, 0, 0, 0, 0);
	private final GuiTransform transform = new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0);
	private boolean enabled = true;
	private boolean useMargins = true;
	private boolean useDefaultBG = false;
	private boolean isVolatile = false;
	
	public final GuiScreen parent;
	
	private IGuiPanel popup = null;
	//private IGuiPanel focused = null;
	
	public GuiScreenCanvas(GuiScreen parent)
	{
		this.parent = parent;
	}
    
    @Override
    public void openPopup(@Nonnull IGuiPanel panel)
    {
        panel.getTransform().setParent(rootTransform);
        popup = panel;
        panel.initPanel();
        //forceFocus(panel);
    }
    
    @Override
    public void closePopup()
    {
        popup = null;
        //resetFocus();
    }
    
    @Override
	public IGuiRect getTransform()
	{
		return this.transform;
	}
	
	@Nonnull
	@Override
	public List<IGuiPanel> getChildren()
    {
        return this.guiPanels;
    }
    
	public GuiScreenCanvas useMargins(boolean enable)
    {
        this.useMargins = enable;
        return this;
    }
	
	public GuiScreenCanvas useDefaultBG(boolean enable)
    {
        this.useDefaultBG = enable;
        return this;
    }
    
    public GuiScreenCanvas setVolatile(boolean state)
    {
        this.isVolatile = state;
        return this;
    }
	
	/**
	 * Use initPanel() for embed support
	 */
	@Override
	public final void initGui()
	{
		super.initGui();
		
		initPanel();
	}
	
	@Override
    public void onGuiClosed()
    {
    	super.onGuiClosed();
		
		Keyboard.enableRepeatEvents(false);
    }
	
	@Override
	public void initPanel()
	{
	    rootTransform.w = this.width;
	    rootTransform.h = this.height;
	    transform.setParent(rootTransform);
	    
	    if(useMargins)
        {
            int marginX = BQ_Settings.guiWidth <= 0 ? 16 : Math.max(16, (this.width - BQ_Settings.guiWidth) / 2);
            int marginY = BQ_Settings.guiHeight <= 0 ? 16 : Math.max(16, (this.height - BQ_Settings.guiHeight) / 2);
            transform.getPadding().setPadding(marginX, marginY, marginX, marginY);
		} else
        {
            transform.getPadding().setPadding(0, 0, 0, 0);
        }
		
		this.guiPanels.clear();
        Arrays.fill(mBtnState, false); // Reset mouse states // TODO: See if I can just make this static across all GUIs
        
	    if(popup != null)
        {
            popup = null;
        }
	}
	
	@Override
	public void setEnabled(boolean state)
	{
		// Technically supported if you wanted something like a multiscreen where this isn't actually the root screen
		this.enabled = state;
	}
	
	@Override
	public boolean isEnabled()
	{
		return this.enabled;
	}
	
	/**
	 * Use initPanel() for embed support
	 */
	@Override
	public final void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(useDefaultBG) this.drawDefaultBackground();
		
		GlStateManager.pushMatrix();
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.disableDepth();
		
		this.drawPanel(mx, my, partialTick);
		
		List<String> tt = getTooltip(mx, my);
		
		if(tt != null && tt.size() > 0)
		{
			this.drawHoveringText(tt, mx, my);
		}
		
		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}
	
	/**
	 * Use panel buttons and the event broadcaster
	 */
	@Override
	@Deprecated
	public void actionPerformed(GuiButton button)
	{
	}
	
	// Remembers the last mouse buttons states. Required to fire release events
	private boolean[] mBtnState = new boolean[3];
	
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		
        int i = Mouse.getEventX() * width / mc.displayWidth;
        int j = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int k = Mouse.getEventButton();
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        boolean flag = Mouse.getEventButtonState();
        
        if(k >= 0 && k < 3 && mBtnState[k] != flag)
        {
        	if(flag)
        	{
        		this.onMouseClick(i, j, k);
        	} else
        	{
        		this.onMouseRelease(i, j, k);
        	}
        	mBtnState[k] = flag;
        }
        
        if(SDX != 0)
        {
        	this.onMouseScroll(i, j, SDX);
        }
	}
	
	@Override
    public void keyTyped(char c, int keyCode)
    {
        if (keyCode == 1) // ESCAPE
        {
        	if(this.isVolatile || this instanceof IVolatileScreen)
        	{
        	    openPopup(new PopChoice(QuestTranslation.translate("betterquesting.gui.closing_warning") + "\n\n" + QuestTranslation.translate("betterquesting.gui.closing_confirm"), PresetIcon.ICON_NOTICE.getTexture(), this::confirmClose, QuestTranslation.translate("gui.yes"), QuestTranslation.translate("gui.no")));
        	} else
			{
				this.mc.displayGuiScreen(null);
				if(this.mc.currentScreen == null) this.mc.setIngameFocus();
			}
			
			return;
        }
        if (keyCode == 14) { // BACKSPACE
            if (this.mc.currentScreen instanceof GuiScreenCanvas) {
                GuiScreenCanvas canvas = (GuiScreenCanvas) mc.currentScreen;
                if (!(canvas instanceof GuiQuestSearch) || !((GuiQuestSearch) canvas).isSearchFocused()) {
                    if (canvas.parent != null) {
                        mc.displayGuiScreen(canvas.parent);
                        return;
                    }
                }
            }
        }
        
        this.onKeyTyped(c, keyCode);
    }
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		for(IGuiPanel entry : guiPanels)
		{
			if(entry.isEnabled())
			{
				entry.drawPanel(mx, my, partialTick);
			}
		}
		
		if(popup != null && popup.isEnabled())
        {
            popup.drawPanel(mx, my, partialTick);
        }
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		boolean used = false;
		
		if(popup != null && popup.isEnabled())
        {
            popup.onMouseClick(mx, my, click);
            return true;// Regardless of whether this is actually used we prevent other things from being edited
        }
		
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(entry.isEnabled() && entry.onMouseClick(mx, my, click))
			{
				used = true;
				break;
			}
		}
		
		return used;
	}
	
	@Override
	public boolean onMouseRelease(int mx, int my, int click)
	{
		boolean used = false;
		
		if(popup != null && popup.isEnabled())
        {
            popup.onMouseRelease(mx, my, click);
            return true;// Regardless of whether this is actually used we prevent other things from being edited
        }
		
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(entry.isEnabled() && entry.onMouseRelease(mx, my, click))
			{
				used = true;
				break;
			}
		}
		
		return used;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		boolean used = false;
		
		if(popup != null && popup.isEnabled())
        {
            popup.onMouseScroll(mx, my, scroll);
            return true;// Regardless of whether this is actually used we prevent other things from being edited
        }
		
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(entry.isEnabled() && entry.onMouseScroll(mx, my, scroll))
			{
				used = true;
				break;
			}
		}
		
		return used;
	}
	
	@Override
	public boolean onKeyTyped(char c, int keycode)
	{
		boolean used = false;
		
		if(popup != null)
        {
            if(popup.isEnabled())
            {
                popup.onKeyTyped(c, keycode);
                return true;// Regardless of whether this is actually used we prevent other things from being edited
            }
        }
		
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(entry.isEnabled() && entry.onKeyTyped(c, keycode))
			{
				used = true;
				break;
			}
		}
		
		if(!used && (BQ_Keybindings.openQuests.getKeyCode() == keycode || mc.gameSettings.keyBindInventory.getKeyCode() == keycode))
		{
        	if(this.isVolatile || this instanceof IVolatileScreen)
        	{
        	    openPopup(new PopChoice(QuestTranslation.translate("betterquesting.gui.closing_warning") + "\n\n" + QuestTranslation.translate("betterquesting.gui.closing_confirm"), PresetIcon.ICON_NOTICE.getTexture(), this::confirmClose, QuestTranslation.translate("gui.yes"), QuestTranslation.translate("gui.no")));
        	} else
			{
				this.mc.displayGuiScreen(null);
				if(this.mc.currentScreen == null) this.mc.setIngameFocus();
			}
		}
		
		return used;
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		List<String> tt;
		
		if(popup != null && popup.isEnabled())
        {
            tt = popup.getTooltip(mx, my);
            if(tt != null) return tt;
        }
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			if(!entry.isEnabled()) continue;
			
			tt = entry.getTooltip(mx, my);
			if(tt != null) return tt;
		}
		
		return null;
	}
	
	@Override
	public void addPanel(IGuiPanel panel)
	{
		if(panel == null || guiPanels.contains(panel))
		{
			return;
		}
		
		guiPanels.add(panel);
		guiPanels.sort(ComparatorGuiDepth.INSTANCE);
		panel.getTransform().setParent(getTransform());
		panel.initPanel();
	}
	
	@Override
	public boolean removePanel(IGuiPanel panel)
	{
		return guiPanels.remove(panel);
	}
	
	@Override
	public void resetCanvas()
	{
		guiPanels.clear();
	}
	
	@Override
    public boolean doesGuiPauseGame()
    {
        return false; // Halts packet handling if paused
    }
	
	@Override
    protected void renderToolTip(ItemStack stack, int x, int y)
    {
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        RenderUtils.drawHoveringText(stack, this.getItemToolTip(stack), x, y, width, height, -1, (font == null ? fontRenderer : font));
    }
	
	@Override
    protected void drawHoveringText(List<String> textLines, int x, int y, @Nonnull FontRenderer font)
    {
        RenderUtils.drawHoveringText(textLines, x, y, width, height, -1, font);
    }
	
	private void confirmClose(int id)
    {
        if(id == 0)
        {
            this.mc.displayGuiScreen(null);
            if(this.mc.currentScreen == null) this.mc.setIngameFocus();
        }
    }
}
