package betterquesting.client.ui_builder;

import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.storage.SimpleDatabase;
import net.minecraft.client.gui.GuiScreen;

import java.util.ListIterator;

public class GuiBuilderMain extends GuiScreenCanvas implements IVolatileScreen
{
    // Database of panel components given unique IDs
    // We can deal with the save/load here without having to make an entire class for this
    private final SimpleDatabase<ComponentPanel> COM_DB = new SimpleDatabase<>();
    // GUI panel representations of components. IDs should line up with COM_DB
    private final SimpleDatabase<IGuiPanel> PANEL_DB = new SimpleDatabase<>();
    
    private CanvasEmpty cvPreview;
    
    private int toolMode = 0;
    // 0 = NONE (operate the preview panels as-is)
    // 1 = SELECT (highlight panels to edit/delete)
    // 2 = CREATE (Places a new panel on to the first panel under the mouse or which-ever is selected)
    // 3 = RESIZE (Drag edges of the panels to change the anchors and padding)
    // 4 = RE-ANCHOR (Switches)
    
    private int selectedID = -1;
    
    // TODO: Add context information about what this GUI is being built for
    public GuiBuilderMain(GuiScreen parent)
    {
        super(parent);
        
        // We're using the entire screen including areas normally reserved for margins to make space for tools
        this.useMargins(false);
        this.useDefaultBG(true);
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        // The normal area with margins will now be the inner preview so we recalculate margins for that here
        int marginX = BQ_Settings.guiWidth <= 0 ? 16 : Math.max(16, (this.width - BQ_Settings.guiWidth) / 2);
        int marginY = BQ_Settings.guiHeight <= 0 ? 16 : Math.max(16, (this.height - BQ_Settings.guiHeight) / 2);
        GuiTransform pvTransform = new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(marginX, marginY, marginX, marginY), 0);
        
        cvPreview = new CanvasEmpty(pvTransform);
        
        // === EXIT CORNER ===
        
        PanelButton btnExit = new PanelButton(new GuiTransform(GuiAlign.TOP_RIGHT, -16, 0, 16, 16, 0), -1, "")
        {
            @Override
            public void onButtonClick()
            {
                mc.displayGuiScreen(parent);
            }
        }.setIcon(PresetIcon.ICON_CROSS.getTexture());
        this.addPanel(btnExit);
        
        // === SAVE - LOAD - REFRESH ===
        
        PanelButton btnSave = new PanelButton(new GuiRectangle(0, 0, 16, 16, 0), 1, "")
        {
            @Override
            public void onButtonClick()
            {
                // Deal with this later
            }
        }.setIcon(PresetIcon.ICON_TICK.getTexture());
        this.addPanel(btnSave);
        
        PanelButton btnLoad = new PanelButton(new GuiRectangle(16, 0, 16, 16, 0), 1, "")
        {
            @Override
            public void onButtonClick()
            {
                // Deal with this later
            }
        }.setIcon(PresetIcon.ICON_FOLDER_OPEN.getTexture());
        this.addPanel(btnLoad);
        
        PanelButton btnRefresh = new PanelButton(new GuiRectangle(32, 0, 16, 16, 0), 1, "")
        {
            @Override
            public void onButtonClick()
            {
            }
        }.setIcon(PresetIcon.ICON_REFRESH.getTexture());
        this.addPanel(btnRefresh);
        
        // === TOOL ROW ===
        
        PanelButton btnAdd = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, 0, -16, 16, 16, 0), 0, "")
        {
            @Override
            public void onButtonClick()
            {
            }
        }.setIcon(PresetIcon.ICON_POSITIVE.getTexture());
        this.addPanel(btnAdd);
    }
    
    @Override
    public void drawPanel(int mx, int my, float partialTick)
    {
        super.drawPanel(mx, my, partialTick);
    }
    
    @Override
    public boolean onMouseClick(int mx, int my, int click)
    {
        if(click != 0 || cvPreview.getChildren().size() <= 0) return super.onMouseClick(mx, my, click);
        
        IGuiPanel topPanel = getPanelUnderMouse(mx, my);
        
        return super.onMouseClick(mx, my, click);
    }
    
    @Override
    public boolean onKeyTyped(char c, int keycode)
    {
        return super.onKeyTyped(c, keycode);
    }
    
    private IGuiPanel getPanelUnderMouse(int mx, int my)
    {
        ListIterator<IGuiPanel> iter = cvPreview.getChildren().listIterator(cvPreview.getChildren().size());
        IGuiPanel topPanel = null;
        
        while(iter.hasPrevious())
        {
            IGuiPanel pan = iter.previous();
            if(pan.getTransform().contains(mx, my)) // Note: We don't care if this is clickable or not. We just care about the bounds
            {
                topPanel = pan;
                if(pan instanceof IGuiCanvas)
                {
                    iter = ((IGuiCanvas)pan).getChildren().listIterator(((IGuiCanvas)pan).getChildren().size());
                    continue;
                }
                break;
            }
        }
        
        return topPanel;
    }
}
