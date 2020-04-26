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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

// This will probably be rewritten at a later date once I reimplement Minecraft's inventory controls natively into their own isolated canvas elements
public class GuiContainerCanvas<T extends Container> extends ContainerScreen<T> implements IScene
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
	//private IGuiPanel focused = null;
	
    public GuiContainerCanvas(Screen parent, T container)
    {
        super(container, Minecraft.getInstance().player.inventory, new StringTextComponent("BQ CONTAINER SCREEN"));
        this.parent = parent;
    }
    
    @Override
    public void openPopup(@Nonnull IGuiPanel panel)
    {
        this.popup = panel;
        //forceFocus(panel);
    }
    
    @Override
    public void closePopup()
    {
        this.popup = null;
        //resetFocus();
    }
	
	@Override
	public IGuiRect getTransform()
	{
		return transform;
	}
	
	@Nonnull
	@Override
	public List<IGuiPanel> getChildren()
    {
        return this.guiPanels;
    }
    
	public GuiContainerCanvas useMargins(boolean enable)
    {
        this.useMargins = enable;
        return this;
    }
	
	public GuiContainerCanvas useDefaultBG(boolean enable)
    {
        this.useDefaultBG = enable;
        return this;
    }
    
    public GuiContainerCanvas setVolatile(boolean state)
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
		
		// Make the container somewhat behave using the root transform bounds
		this.guiLeft = 0;
		this.guiTop = 0;
		this.xSize = width;
		this.ySize = height;
		
		initPanel();
	}
	
	@Override
    public void onClose()
    {
    	super.onClose();
		
		Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
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
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mx, int my)
    {
		if(useDefaultBG) this.renderBackground();
		
		GlStateManager.pushMatrix();
		GlStateManager.color4f(1F, 1F, 1F, 1F);
		GlStateManager.disableDepthTest();
		
		this.drawPanel(mx, my, partialTick);
		
		GlStateManager.enableDepthTest();
		GlStateManager.popMatrix();
    }
    
    @Override
    public void render(int mx, int my, float partialTick)
    {
        super.render(mx, my, partialTick);
		
		List<String> tt = this.getTooltip(mx, my);
		if(tt != null && tt.size() > 0) this.renderTooltip(tt, mx, my, font);
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
    public boolean keyPressed(int keyCode, int scancode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE)
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
        
        return super.keyPressed(keyCode, scancode, modifiers) || this.onKeyPressed(keyCode, scancode, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scancode, int modifiers)
    {
        return super.keyReleased(keyCode, scancode, modifiers) || this.onKeyRelease(keyCode, scancode, modifiers);
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
		
		while(pnIter.hasPrevious()) // TODO: Allow click through even after used. Other panels need it to passively reset things
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
		
		while(pnIter.hasPrevious()) // TODO: Allow click through even after used. Other panels need it to passively reset things
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
	
	//@Override
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
		List<String> tt = null;
		
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
			if(tt != null && tt.size() > 0) return tt;
		}
		
		if(tt == null)
        {
            for(Slot slot : getContainer().inventorySlots)
            {
                if(slot.isEnabled() && slot.getHasStack() && isPointInRegion(slot.xPos, slot.yPos, 16, 16, mx, my))
                {
                    tt = convertComponents(slot.getStack().getTooltip(minecraft.player, minecraft.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
                    return tt.size() <= 0 ? null : tt;
                }
            }
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
        RenderUtils.drawHoveringText(stack, this.getTooltipFromItem(stack), x, y, width, height, -1, (itemFont == null ? font : itemFont));
    }
	
	@Override
    public void renderTooltip(List<String> textLines, int x, int y, FontRenderer itemFont)
    {
        RenderUtils.drawHoveringText(textLines, x, y, width, height, -1, itemFont);
    }
	
	public void confirmClose(int id)
    {
        if(id == 0)
        {
            this.minecraft.displayGuiScreen(null);
            if(this.minecraft.currentScreen == null) this.minecraft.setGameFocused(true);
        }
    }
    
    private List<String> convertComponents(List<ITextComponent> comList)
    {
        if(comList == null || comList.size() == 0) return Collections.emptyList();
        
        List<String> list = new ArrayList<>();
        comList.forEach((com) -> list.add(com.getFormattedText()));
        return list;
    }
}
