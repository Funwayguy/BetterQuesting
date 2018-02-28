package betterquesting.client.gui2;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.client.gui.GuiQuestInstance;
import betterquesting.client.gui.GuiQuestLinesMain;
import betterquesting.client.gui.editors.GuiQuestLineEditorA;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class GuiQuestLines extends GuiScreenCanvas implements IPEventListener
{
    private IQuestLine selectedLine = null;
    
    private CanvasQuestLine cvQuest;
    private PanelTextBox paDesc;
    
    public GuiQuestLines(GuiScreen parent)
    {
        super(parent);
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
    
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
        
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 100, 16, 0), 0, "Back"));
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -16, 100, 16, 0), 3, "Edit"));
    
        CanvasScrolling cvList = new CanvasScrolling(new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(16, 16, -136, 16), 0));
        cvBackground.addPanel(cvList);
        
        CanvasTextured cvFrame = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(152, 16, 16, 66), 0), PresetTexture.AUX_FRAME_0.getTexture());
        cvBackground.addPanel(cvFrame);
        
        cvQuest = new CanvasQuestLine(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), 2);
        cvFrame.addPanel(cvQuest);
    
        List<IQuestLine> lineList = QuestLineDatabase.INSTANCE.getAllValues();
    
        for(int i = 0; i < lineList.size(); i++)
        {
            IQuestLine ql = lineList.get(i);
            PanelButtonStorage<IQuestLine> btnLine = new PanelButtonStorage<IQuestLine>(new GuiRectangle(0, i * 16, 120, 16, 0), 1, I18n.format(ql.getUnlocalisedName()), ql);
            
            if(ql == selectedLine)
            {
                //btnLine.setEnabled(false);
            }
            
            cvList.addPanel(btnLine);
        }
        
        paDesc = new PanelTextBox(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(152, -66, 16, 16), 0), "");
        paDesc.setColor(PresetColor.TEXT_MAIN.getColor());
        cvBackground.addPanel(paDesc);
        
        // === DECORATIVE LINES ===
    
        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_LEFT, 16, 16, 0, 0, 0);
        ls0.setParent(cvBackground.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.TOP_LEFT, 136, 16, 0, 0, 0);
        le0.setParent(cvBackground.getTransform());
        PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), -1);
        cvBackground.addPanel(paLine0);
        
        IGuiRect ls1 = new GuiTransform(GuiAlign.BOTTOM_LEFT, 16, -16, 0, 0, 0);
        ls1.setParent(cvBackground.getTransform());
        IGuiRect le1 = new GuiTransform(GuiAlign.BOTTOM_LEFT, 136, -16, 0, 0, 0);
        le1.setParent(cvBackground.getTransform());
        PanelLine paLine1 = new PanelLine(ls1, le1, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvBackground.addPanel(paLine1);
    
        IGuiRect ls2 = new GuiTransform(GuiAlign.TOP_LEFT, 144, 16, 0, 0, 0);
        ls2.setParent(cvBackground.getTransform());
        IGuiRect le2 = new GuiTransform(GuiAlign.BOTTOM_LEFT, 144, -16, 0, 0, 0);
        le2.setParent(cvBackground.getTransform());
        PanelLine paLine2 = new PanelLine(ls2, le2, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvBackground.addPanel(paLine2);
    
        IGuiRect ls3 = new GuiTransform(GuiAlign.BOTTOM_LEFT, 152, -16, 0, 0, 0);
        ls3.setParent(cvBackground.getTransform());
        IGuiRect le3 = new GuiTransform(GuiAlign.BOTTOM_RIGHT, -16, -16, 0, 0, 0);
        le3.setParent(cvBackground.getTransform());
        PanelLine paLine3 = new PanelLine(ls3, le3, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvBackground.addPanel(paLine3);
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
        Minecraft mc = Minecraft.getMinecraft();
        IPanelButton btn = event.getButton();
        
        if(btn.getButtonID() == 0) // Exit
        {
            mc.displayGuiScreen(this.parent);
        } else if(btn.getButtonID() == 1 && btn instanceof PanelButtonStorage) // Quest Line Select
        {
            @SuppressWarnings("unchecked")
            IQuestLine ql = ((PanelButtonStorage<IQuestLine>)btn).getStoredValue();
            selectedLine = ql;
            cvQuest.setQuestLine(ql);
            paDesc.setText(I18n.format(ql.getUnlocalisedDescription()));
            btn.setEnabled(false) ;
        } else if(btn.getButtonID() == 2 && btn instanceof PanelButtonStorage) // Quest Instance Select
        {
            @SuppressWarnings("unchecked")
            IQuest quest = ((PanelButtonStorage<IQuest>)btn).getStoredValue();
            GuiQuestLinesMain.bookmarked = new GuiQuestInstance(this, quest);
            mc.displayGuiScreen(GuiQuestLinesMain.bookmarked);
        } else if(btn.getButtonID() == 3)
        {
            mc.displayGuiScreen(new GuiQuestLineEditorA(this));
        }
    }
}
