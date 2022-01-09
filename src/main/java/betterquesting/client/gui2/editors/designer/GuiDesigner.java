package betterquesting.client.gui2.editors.designer;

import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.client.toolbox.IToolTab;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.toolbox.ToolboxRegistry;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class GuiDesigner extends GuiScreenCanvas implements IVolatileScreen, INeedsRefresh, IPEventListener
{
    // Not final because I hope to support hot swapping in future
    private IQuestLine questLine;
    private final int lineID;
    
    private PanelToolController toolController;
    private IGuiCanvas cvTray;
    
    private final List<IToolTab> tabList = new ArrayList<>();
    private PanelTextBox tabTitle;
    private IGuiPanel lastTabPanel;
    private int tabIdx = 0;
    
    private CanvasQuestLine cvQuest;
    
    public GuiDesigner(GuiScreen parent, IQuestLine line)
    {
        super(parent);
        this.questLine = line;
        this.lineID = QuestLineDatabase.INSTANCE.getID(line);
        this.tabList.addAll(ToolboxRegistry.INSTANCE.getAllTabs());
    }
    
    @Override
    public void refreshGui()
    {
        this.questLine = QuestLineDatabase.INSTANCE.getValue(lineID);
        
        if(questLine == null)
        {
            mc.displayGuiScreen(parent);
            return;
        }
        
        int sx = cvQuest.getScrollX();
        int sy = cvQuest.getScrollY();
        float z = cvQuest.getZoom();
        cvQuest.setQuestLine(questLine);
        cvQuest.setZoom(z); // Always set the scale before attempting to scroll
        cvQuest.setScrollX(sx);
        cvQuest.setScrollY(sy);
        
        toolController.refreshCanvas();
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
		PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        Keyboard.enableRepeatEvents(true);
        
        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 96, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
        
        cvTray = new CanvasTextured(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-96, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvTray);
        
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.done")));
        
        PanelGeneric pnFrame = new PanelGeneric(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0), PresetTexture.AUX_FRAME_0.getTexture());
        cvBackground.addPanel(pnFrame);
        
        cvQuest = new CanvasQuestLine(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0), 1);
        cvBackground.addPanel(cvQuest);
        cvQuest.setQuestLine(questLine);
        
        PanelButton btnTabLeft = new PanelButton(new GuiTransform(new Vector4f(0F, 0F, 0.5F, 0F), new GuiPadding(16, 32, 0, -40), 0), 2, "")
        {
            @Override
            public void onButtonClick()
            {
                tabIdx--;
                refreshToolTab();
            }
        };
        btnTabLeft.setIcon(PresetIcon.ICON_LEFT.getTexture());
        cvTray.addPanel(btnTabLeft);
        
        PanelButton btnTabRight = new PanelButton(new GuiTransform(new Vector4f(0.5F, 0F, 1F, 0F), new GuiPadding(0, 32, 16, -40), 0), 3, "")
        {
            @Override
            public void onButtonClick()
            {
                tabIdx++;
                refreshToolTab();
            }
        };
        btnTabRight.setIcon(PresetIcon.ICON_RIGHT.getTexture());
        cvTray.addPanel(btnTabRight);
        
        tabTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 20, 16, -32), 0), "?").setAlignment(1).setColor(PresetColor.TEXT_HEADER.getColor());
        cvTray.addPanel(tabTitle);
        
        if(toolController != null)
        {
            cvBackground.addPanel(toolController);
            cvQuest.setScrollDriverX(toolController.getScrollX());
            cvQuest.setScrollDriverY(toolController.getScrollY());
            toolController.changeCanvas(cvQuest);
        } else
        {
            toolController = new PanelToolController(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), -1), cvQuest);
            cvBackground.addPanel(toolController);
            cvQuest.setScrollDriverX(toolController.getScrollX());
            cvQuest.setScrollDriverY(toolController.getScrollY());
            cvQuest.fitToWindow();
        }
        
        refreshToolTab();
    }
    
    private void refreshToolTab()
    {
        if(lastTabPanel != null) cvTray.removePanel(lastTabPanel);
        
        if(tabList.size() <= 0) return;
        if(tabIdx < 0) while(tabIdx < 0) tabIdx += tabList.size();
        tabIdx %= tabList.size();
        
        lastTabPanel = tabList.get(tabIdx).getTabGui(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 48, 16, 16), 0), cvQuest, toolController);
        tabTitle.setText(QuestTranslation.translate(tabList.get(tabIdx).getUnlocalisedName()));
        
        if(lastTabPanel != null) cvTray.addPanel(lastTabPanel);
    }
	
	@Override
	public void onPanelEvent(PanelEvent event)
	{
		if(event instanceof PEventButton)
		{
			onButtonPress((PEventButton)event);
		}
	}
	
	private void onButtonPress(PEventButton event)
    {
        IPanelButton btn = event.getButton();
        
        if(btn.getButtonID() == 0) // Exit
        {
            mc.displayGuiScreen(this.parent);
        }
    }
}
