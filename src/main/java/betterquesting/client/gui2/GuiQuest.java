package betterquesting.client.gui2;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.PanelLegacyEmbed;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.client.gui.editors.GuiQuestEditor;
import betterquesting.questing.QuestDatabase;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.util.vector.Vector4f;

public class GuiQuest extends GuiScreenCanvas implements IPEventListener
{
    private final int questID;
    
    private IQuest quest;
    
    private PanelButton btnTaskLeft;
    private PanelButton btnTaskRight;
    private PanelButton btnRewardLeft;
    private PanelButton btnRewardRight;
    
    private PanelButton btnDetect;
    private PanelButton btnClaim;
    
    public GuiQuest(GuiScreen parent, int questID)
    {
        super(parent);
        this.questID = questID;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        this.quest = QuestDatabase.INSTANCE.getValue(questID);
    
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
    
        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
    
        PanelTextBox panTxt = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), I18n.format(quest.getUnlocalisedName())).setAlignment(1);
        panTxt.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(panTxt);
    
        if(QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(mc.player))
        {
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 100, 16, 0), 0, I18n.format("gui.back")));
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -16, 100, 16, 0), 3, I18n.format("betterquesting.btn.edit")));
        } else
        {
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, I18n.format("gui.back")));
        }
    
        CanvasEmpty cvInner = new CanvasEmpty(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 32, 16, 32), 0));
        cvBackground.addPanel(cvInner);
        
        if(quest.getRewards().size() > 0)
        {
            CanvasScrolling cvDesc = new CanvasScrolling(new GuiTransform(new Vector4f(0F, 0F, 0.5F, 0.5F), new GuiPadding(0, 0, 16, 0), 0));
            cvInner.addPanel(cvDesc);
            PanelTextBox paDesc = new PanelTextBox(new GuiRectangle(0, 0, cvDesc.getTransform().getWidth(), 0), I18n.format(quest.getUnlocalisedDescription()), true);
            cvDesc.addPanel(paDesc);
    
            PanelVScrollBar paDescScroll = new PanelVScrollBar(new GuiTransform(GuiAlign.quickAnchor(GuiAlign.TOP_CENTER, GuiAlign.MID_CENTER), new GuiPadding(-16, 0, 8, 0), 0));
            cvInner.addPanel(paDescScroll);
            cvDesc.setScrollDriverY(paDescScroll);
        } else
        {
            CanvasScrolling cvDesc = new CanvasScrolling(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(0, 0, 16, 0), 0));
            cvInner.addPanel(cvDesc);
            PanelTextBox paDesc = new PanelTextBox(new GuiRectangle(0, 0, cvDesc.getTransform().getWidth(), 0), I18n.format(quest.getUnlocalisedDescription()), true);
            cvDesc.addPanel(paDesc);
    
            PanelVScrollBar paDescScroll = new PanelVScrollBar(new GuiTransform(GuiAlign.quickAnchor(GuiAlign.TOP_CENTER, GuiAlign.BOTTOM_CENTER), new GuiPadding(-16, 0, 8, 0), 0));
            cvInner.addPanel(paDescScroll);
            cvDesc.setScrollDriverY(paDescScroll);
        }
        
        
        if(quest.getTasks().size() > 0)
        {
            IGuiRect rectEmb = new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 0, 0, 16), 0);
            rectEmb.setParent(cvInner.getTransform());
            PanelLegacyEmbed paEmb = new PanelLegacyEmbed(rectEmb, quest.getTasks().getAllValues().get(0).getTaskGui(rectEmb.getX(), rectEmb.getY(), rectEmb.getWidth(), rectEmb.getHeight(), quest));
            cvInner.addPanel(paEmb);
        }
    
        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 0, 0, 0, 0);
        ls0.setParent(cvInner.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, 0, 0, 0, 0);
        le0.setParent(cvInner.getTransform());
        PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvInner.addPanel(paLine0);
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
        } else if(btn.getButtonID() == 3)
        {
            mc.displayGuiScreen(new GuiQuestEditor(this, quest));
        }
    }
}
