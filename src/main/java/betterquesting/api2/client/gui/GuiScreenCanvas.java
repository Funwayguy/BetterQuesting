package betterquesting.api2.client.gui;

import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.popups.PopChoice;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.BQ_Keybindings;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuiScreenCanvas extends Screen implements IScene
{
	private final List<IGuiPanel> guiPanels = new CopyOnWriteArrayList<>();
	private final GuiRectangle rootTransform = new GuiRectangle(0, 0, 0, 0, 0);
	private final GuiTransform transform = new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0);
	private boolean enabled = true;
	private boolean useMargins = true;
	private boolean useDefaultBG = false;
	private boolean isVolatile = false;
	
	public final Screen parent;
	
	private IGuiPanel popup = null;
	
	public GuiScreenCanvas(Screen parent)
	{
	    super(new StringTextComponent("BQ SCREEN"));
		this.parent = parent;
	}
    
    @Override
    public void openPopup(@Nonnull IGuiPanel panel)
    {
        panel.getTransform().setParent(rootTransform);
        popup = panel;
        panel.initPanel();
    }
    
    @Override
    public void closePopup()
    {
        popup = null;
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
	public final void init()
	{
		super.init();
		
		initPanel();
	}
	
	@Override
    public void onClose()
    {
    	super.onClose();
		
    	this.minecraft.keyboardListener.enableRepeatEvents(false);
    }
    
    @Override
    public void setSize(int w, int h)
    {
        super.setSize(w, h);
        rootTransform.w = w;
        rootTransform.h = h;
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
        
	    if(popup != null) popup = null;
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
	public final void render(int mx, int my, float partialTick)
	{
		super.render(mx, my, partialTick);
		
		if(useDefaultBG) this.renderBackground();
		
		GlStateManager.pushMatrix();
		GlStateManager.color4f(1F, 1F, 1F, 1F);
		GlStateManager.disableDepthTest();
		
		this.drawPanel(mx, my, partialTick);
		
		List<String> tt = getTooltip(mx, my);
		
		if(tt != null && tt.size() > 0)
		{
			this.renderTooltip(tt, mx, my);
		}
		
		GlStateManager.enableDepthTest();
		GlStateManager.popMatrix();
	}
	
	@Override
    public boolean mouseClicked(double mx, double my, int button)
    {
        return super.mouseClicked(mx, my, button) || this.onMouseClick((int)Math.floor(mx), (int)Math.floor(my), button);
    }
    
    @Override
    public boolean mouseReleased(double mx, double my, int button)
    {
        return super.mouseReleased(mx, my, button) || this.onMouseRelease((int)Math.floor(mx), (int)Math.floor(my), button);
    }
	
	@Override
    public boolean mouseScrolled(double mx, double my, double scroll)
    {
        return scroll != 0 && (super.mouseScrolled(mx, my, scroll) || this.onMouseScroll((int)Math.floor(mx), (int)Math.floor(my), (int)Math.ceil(scroll)));
    }
	
	@Override
    public boolean keyPressed(int keycode, int scancode, int modifiers)
    {
        if (keycode == GLFW.GLFW_KEY_ESCAPE)
        {
        	if(this.isVolatile)
        	{
        	    openPopup(new PopChoice(QuestTranslation.translate("betterquesting.gui.closing_warning") + "\n\n" + QuestTranslation.translate("betterquesting.gui.closing_confirm"), PresetIcon.ICON_NOTICE.getTexture(), this::confirmClose, QuestTranslation.translate("gui.yes"), QuestTranslation.translate("gui.no")));
        	} else
			{
				this.minecraft.displayGuiScreen(null);
				if(this.minecraft.currentScreen == null) this.minecraft.setGameFocused(true);
			}
			
			return true;
        }
        
        return super.keyPressed(keycode, scancode, modifiers) || this.onKeyPressed(keycode, scancode, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keycode, int scancode, int modifiers)
    {
        return super.keyReleased(keycode, scancode, modifiers) || this.onKeyRelease(keycode, scancode, modifiers);
    }
    
    @Override
    public boolean charTyped(char c, int keycode)
    {
        return super.charTyped(c, keycode) || this.onCharTyped(c, keycode);
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
	public boolean onKeyPressed(int keycode, int scancode, int modifiers)
	{
		boolean used = false;
		
		if(popup != null)
        {
            if(popup.isEnabled())
            {
                popup.onKeyPressed(keycode, scancode, modifiers);
                return true;// Regardless of whether this is actually used we prevent other things from being edited
            }
        }
		
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(entry.isEnabled() && entry.onKeyPressed(keycode, scancode, modifiers))
			{
				used = true;
				break;
			}
		}
		
		if(!used && (BQ_Keybindings.openQuests.getKey().getKeyCode() == keycode || minecraft.gameSettings.keyBindInventory.getKey().getKeyCode() == keycode))
		{
        	if(this.isVolatile)
        	{
        	    openPopup(new PopChoice(QuestTranslation.translate("betterquesting.gui.closing_warning") + "\n\n" + QuestTranslation.translate("betterquesting.gui.closing_confirm"), PresetIcon.ICON_NOTICE.getTexture(), this::confirmClose, QuestTranslation.translate("gui.yes"), QuestTranslation.translate("gui.no")));
        	} else
			{
				this.minecraft.displayGuiScreen(null);
				if(this.minecraft.currentScreen == null) this.minecraft.setGameFocused(true);
			}
        	used = true;
		}
		
		return used;
	}
	
	@Override
    public boolean onKeyRelease(int keycode, int scancode, int modifiers)
    {
		boolean used = false;
		
		if(popup != null)
        {
            if(popup.isEnabled())
            {
                popup.onKeyRelease(keycode, scancode, modifiers);
                return true;// Regardless of whether this is actually used we prevent other things from being edited
            }
        }
		
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(entry.isEnabled() && entry.onKeyRelease(keycode, scancode, modifiers))
			{
				used = true;
				break;
			}
		}
		
		return used;
    }
    
    @Override
    public boolean onCharTyped(char c, int keycode)
    {
		boolean used = false;
		
		if(popup != null)
        {
            if(popup.isEnabled())
            {
                popup.onCharTyped(c, keycode);
                return true;// Regardless of whether this is actually used we prevent other things from being edited
            }
        }
		
		ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
		
		while(pnIter.hasPrevious())
		{
			IGuiPanel entry = pnIter.previous();
			
			if(entry.isEnabled() && entry.onCharTyped(c, keycode))
			{
				used = true;
				break;
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
    public boolean isPauseScreen()
    {
        return false; // Halts packet handling if paused
    }
	
	@Override
    protected void renderTooltip(ItemStack stack, int x, int y)
    {
        FontRenderer itemFont = stack.getItem().getFontRenderer(stack);
        RenderUtils.drawHoveringText(stack, this.getTooltipFromItem(stack), x, y, width, height, -1, (itemFont == null ? this.font : itemFont));
    }
	
	@Override
    public void renderTooltip(List<String> textLines, int x, int y, @Nonnull FontRenderer itemFont)
    {
        RenderUtils.drawHoveringText(textLines, x, y, width, height, -1, itemFont);
    }
	
	private void confirmClose(int id)
    {
        if(id == 0 && this.minecraft != null)
        {
            this.minecraft.displayGuiScreen(null);
            if(this.minecraft.currentScreen == null) this.minecraft.setGameFocused(true);
        }
    }
}
