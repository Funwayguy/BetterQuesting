package betterquesting.client.gui2;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumQuestState;
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
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.client.gui.GuiQuestInstance;
import betterquesting.client.gui.editors.GuiQuestLineEditorA;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.util.List;
import java.util.UUID;

public class GuiQuestLines extends GuiScreenCanvas implements IPEventListener
{
    private IQuestLine selectedLine = null;
    private int selectedLineId = -1;
    private int lastScrollX = 0;
    private int lastScrollY = 0;
    private float lastZoom = 1F;
    
    private PanelButtonStorage[] qlBtns;
    private CanvasQuestLine cvQuest;
    private CanvasScrolling cvDesc;
    private PanelTextBox paDesc;
    
    public GuiQuestLines(GuiScreen parent)
    {
        super(parent);
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        if(selectedLineId >= 0)
        {
            selectedLine = QuestLineDatabase.INSTANCE.getValue(selectedLineId);
            
            if(selectedLine == null)
            {
                selectedLineId = -1;
            }
        } else
        {
            selectedLine = null;
        }
        
        boolean canEdit = QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(mc.player);
        
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
        
        if(canEdit)
        {
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 100, 16, 0), 0, I18n.format("gui.back")));
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -16, 100, 16, 0), 3, I18n.format("betterquesting.btn.edit")));
        } else
        {
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, I18n.format("gui.back")));
        }
    
        CanvasScrolling cvList = new CanvasScrolling(new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(16, 16, -158, 16), 0));
        cvBackground.addPanel(cvList);
        PanelVScrollBar pnQScroll = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(0, 0, -8, 0), 0));
        cvList.setScrollDriverY(pnQScroll);
        cvBackground.addPanel(pnQScroll);
        pnQScroll.getTransform().setParent(cvList.getTransform());
    
        List<IQuestLine> lineList = QuestLineDatabase.INSTANCE.getAllValues();
        this.qlBtns = new PanelButtonStorage[lineList.size()];
        UUID playerID = QuestingAPI.getQuestingUUID(mc.player);
    
        for(int i = 0; i < lineList.size(); i++)
        {
            IQuestLine ql = lineList.get(i);
    
            PanelButtonStorage<IQuestLine> btnLine = new PanelButtonStorage<>(new GuiRectangle(0, i * 16, 142, 16, 0), 1, I18n.format(ql.getUnlocalisedName()), ql);
            
            boolean show = canEdit;
            boolean hasUnclaimed = false;
            
            if(!show)
            {
	            for(int qID : ql.getAllKeys())
	            {
	                IQuest q = QuestDatabase.INSTANCE.getValue(qID);
	                if(q == null)
	                {
	                    continue;
	                }
	                
	                if(!show && CanvasQuestLine.isQuestShown(q, playerID))
	                {
	                    show = true;
	                }
	                
	                if(!hasUnclaimed && q.getState(playerID) == EnumQuestState.UNCLAIMED)
	                {
	                    hasUnclaimed = true;
	                }
	                
	                if (show && hasUnclaimed)
	                {
	                    break;
	                }
	            }
            }

            if(!show || ql == selectedLine)
            {
                btnLine.setEnabled(false);
            }
            else if(hasUnclaimed)
            {
                btnLine.setTextHighlight(PresetColor.BTN_DISABLED.getColor(), PresetColor.QUEST_ICON_PENDING.getColor(), PresetColor.BTN_HOVER.getColor());
            }
            
            cvList.addPanel(btnLine);
            qlBtns[i] = btnLine;
        }
    
        CanvasTextured cvFrame = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(174, 16, 16, 66), 0), PresetTexture.AUX_FRAME_0.getTexture());
        cvBackground.addPanel(cvFrame);
    
        cvQuest = new CanvasQuestLine(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), 2);
        cvFrame.addPanel(cvQuest);
        
        if(selectedLine != null)
        {
            cvQuest.setQuestLine(selectedLine);
            cvQuest.setZoom(lastZoom);
            cvQuest.setScrollX(lastScrollX);
            cvQuest.setScrollY(lastScrollY);
        }
        
        cvDesc = new CanvasScrolling(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(174, -66, 24, 16), 0));
        cvBackground.addPanel(cvDesc);
        
        paDesc = new PanelTextBox(new GuiRectangle(0, 0, cvDesc.getTransform().getWidth(), 0, 0), "", true);
        paDesc.setColor(PresetColor.TEXT_MAIN.getColor());
        cvDesc.addPanel(paDesc);
    
        PanelVScrollBar scDesc = new PanelVScrollBar(new GuiTransform(GuiAlign.BOTTOM_RIGHT, new GuiPadding(-24, -66, 16, 16), 0));
        cvDesc.setScrollDriverY(scDesc);
        cvBackground.addPanel(scDesc);
        
        // === DECORATIVE LINES ===
    
        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_LEFT, 16, 16, 0, 0, 0);
        ls0.setParent(cvBackground.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.TOP_LEFT, 166, 16, 0, 0, 0);
        le0.setParent(cvBackground.getTransform());
        PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), -1);
        cvBackground.addPanel(paLine0);
        
        IGuiRect ls1 = new GuiTransform(GuiAlign.BOTTOM_LEFT, 16, -16, 0, 0, 0);
        ls1.setParent(cvBackground.getTransform());
        IGuiRect le1 = new GuiTransform(GuiAlign.BOTTOM_LEFT, 166, -16, 0, 0, 0);
        le1.setParent(cvBackground.getTransform());
        PanelLine paLine1 = new PanelLine(ls1, le1, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvBackground.addPanel(paLine1);
    
        IGuiRect ls3 = new GuiTransform(GuiAlign.BOTTOM_LEFT, 174, -16, 0, 0, 0);
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
            for(PanelButtonStorage b : qlBtns)
            {
                if(b.getStoredValue() == selectedLine)
                {
                    b.setEnabled(true);
                    break;
                }
            }
            
            @SuppressWarnings("unchecked")
            IQuestLine ql = ((PanelButtonStorage<IQuestLine>)btn).getStoredValue();
            selectedLine = ql;
            selectedLineId = QuestLineDatabase.INSTANCE.getKey(ql);
            cvQuest.setQuestLine(ql);
            paDesc.setText(I18n.format(ql.getUnlocalisedDescription()));
            cvDesc.refreshScrollBounds();
            
            btn.setEnabled(false);
        } else if(btn.getButtonID() == 2 && btn instanceof PanelButtonStorage) // Quest Instance Select
        {
            @SuppressWarnings("unchecked")
            IQuest quest = ((PanelButtonStorage<IQuest>)btn).getStoredValue();
            GuiHome.bookmark = new GuiQuestInstance(this, quest);
            this.lastScrollX = cvQuest.getScrollX();
            this.lastScrollY = cvQuest.getScrollY();
            this.lastZoom = cvQuest.getZoom();
            
            //mc.displayGuiScreen(new GuiQuest(this, QuestDatabase.INSTANCE.getKey(quest))); // Unfinished new stuff
            mc.displayGuiScreen(GuiHome.bookmark);
        } else if(btn.getButtonID() == 3)
        {
            mc.displayGuiScreen(new GuiQuestLineEditorA(this));
        }
    }
}
