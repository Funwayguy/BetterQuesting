package betterquesting.client.gui2;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.GuiTextureColored;
import betterquesting.api2.client.gui.resources.textures.OreDictTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.gui2.editors.GuiQuestLinesEditor;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiQuestLines extends GuiScreenCanvas implements IPEventListener, INeedsRefresh
{
    private IQuestLine selectedLine = null;
    private int selectedLineId = -1;
    
    private final List<PanelButtonStorage> qlBtns = new ArrayList<>();
    private CanvasQuestLine cvQuest;
    private CanvasScrolling cvDesc;
    private PanelVScrollBar scDesc;
    private CanvasScrolling cvLines;
    private PanelVScrollBar scLines;
    private PanelTextBox paDesc;
    private PanelButton claimAll;
    
    public GuiQuestLines(GuiScreen parent)
    {
        super(parent);
    }
    
    @Override
    public void refreshGui()
    {
        refreshContent();
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        if(selectedLineId >= 0)
        {
            selectedLine = QuestLineDatabase.INSTANCE.getValue(selectedLineId);
            if(selectedLine == null) selectedLineId = -1;
        } else
        {
            selectedLine = null;
        }
        
        boolean canEdit = QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(mc.thePlayer);
        
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
        
        if(canEdit)
        {
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 100, 16, 0), 0, QuestTranslation.translate("gui.back")));
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -16, 100, 16, 0), 3, QuestTranslation.translate("betterquesting.btn.edit")));
        } else
        {
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.back")));
        }
    
        cvLines = new CanvasScrolling(new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(16, 16, -158, 16), 0));
        cvBackground.addPanel(cvLines);
        scLines = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(0, 0, -8, 0), 0));
        cvLines.setScrollDriverY(scLines);
        cvBackground.addPanel(scLines);
        scLines.getTransform().setParent(cvLines.getTransform());
        
        refreshList();
    
        CanvasTextured cvFrame = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(174, 16, 16, 66), 0), PresetTexture.AUX_FRAME_0.getTexture());
        cvBackground.addPanel(cvFrame);
        
        CanvasQuestLine oldCvQuest = cvQuest;
        cvQuest = new CanvasQuestLine(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), 2);
        cvFrame.addPanel(cvQuest);
        
        cvDesc = new CanvasScrolling(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(174, -66, 24, 16), 0));
        cvBackground.addPanel(cvDesc);
        
        paDesc = new PanelTextBox(new GuiRectangle(0, 0, cvDesc.getTransform().getWidth(), 0, 0), "", true);
        paDesc.setColor(PresetColor.TEXT_MAIN.getColor());//.setFontSize(10);
        cvDesc.addCulledPanel(paDesc, false);
    
        scDesc = new PanelVScrollBar(new GuiTransform(GuiAlign.BOTTOM_RIGHT, new GuiPadding(-24, -66, 16, 16), 0));
        cvDesc.setScrollDriverY(scDesc);
        cvBackground.addPanel(scDesc);
    
        if(selectedLine != null)
        {
            cvQuest.setQuestLine(selectedLine);
            
            if(oldCvQuest != null)
            {
                cvQuest.setZoom(oldCvQuest.getZoom());
                cvQuest.setScrollX(oldCvQuest.getScrollX());
                cvQuest.setScrollY(oldCvQuest.getScrollY());
                cvQuest.refreshScrollBounds();
                cvQuest.updatePanelScroll();
            }
            
            paDesc.setText(QuestTranslation.translate(selectedLine.getUnlocalisedDescription()));
            cvDesc.refreshScrollBounds();
            scDesc.setEnabled(cvDesc.getScrollBounds().getHeight() > 0);
        }
        
        // === SHORTCUTS ===
        
        PanelButton fitView = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_RIGHT, -16, -16, 16, 16, -2), 5, "");
        fitView.setIcon(PresetIcon.ICON_BOX_FIT.getTexture());
        cvFrame.addPanel(fitView);
        
        claimAll = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_RIGHT, -32, -16, 16, 16, -2), 4, "");
        claimAll.setIcon(PresetIcon.ICON_CHEST.getTexture());
        cvFrame.addPanel(claimAll);
        
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
        
        refreshClaimAll();
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
                    b.setActive(true);
                    break;
                }
            }
            
            @SuppressWarnings("unchecked")
            IQuestLine ql = ((PanelButtonStorage<IQuestLine>)btn).getStoredValue();
            selectedLine = ql;
            selectedLineId = QuestLineDatabase.INSTANCE.getID(ql);
            cvQuest.setQuestLine(ql);
            paDesc.setText(QuestTranslation.translate(ql.getUnlocalisedDescription()));
            cvDesc.refreshScrollBounds();
            refreshClaimAll();
            
            scDesc.setEnabled(cvDesc.getScrollBounds().getHeight() > 0);
            
            btn.setActive(false);
        } else if(btn.getButtonID() == 2 && btn instanceof PanelButtonStorage) // Quest Instance Select
        {
            @SuppressWarnings("unchecked")
            DBEntry<IQuest> quest = ((PanelButtonStorage<DBEntry<IQuest>>)btn).getStoredValue();
            GuiHome.bookmark = new GuiQuest(this, quest.getID());
            
            mc.displayGuiScreen(GuiHome.bookmark);
        } else if(btn.getButtonID() == 3)
        {
            mc.displayGuiScreen(new GuiQuestLinesEditor(this));
        } else if(btn.getButtonID() == 4)
        {
            if(cvQuest.getQuestButtons().size() <= 0) return;
            List<Integer> claimIdList = new ArrayList<>();
            for(PanelButtonQuest pbQuest : cvQuest.getQuestButtons())
            {
                IQuest q = pbQuest.getStoredValue().getValue();
                if(q.getRewards().size() > 0 && q.canClaim(mc.thePlayer)) claimIdList.add(pbQuest.getStoredValue().getID());
            }
            
            int[] cIDs = new int[claimIdList.size()];
            for(int i = 0; i < cIDs.length; i++)
            {
                cIDs[i] = claimIdList.get(i);
            }
            
            NBTTagCompound tags = new NBTTagCompound();
            tags.setIntArray("questID", cIDs);
            PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.CLAIM.GetLocation(), tags));
        } else if(btn.getButtonID() == 5)
        {
            if(cvQuest.getQuestLine() == null) return;
            cvQuest.fitToWindow();
            
        }
    }
    
    private void refreshList()
    {
        for(PanelButtonStorage btn : qlBtns)
        {
            cvLines.removePanel(btn);
        }
        
        boolean canEdit = QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(mc.thePlayer);
        DBEntry<IQuestLine>[] lineList = QuestLineDatabase.INSTANCE.getSortedEntries();
        this.qlBtns.clear();
        UUID playerID = QuestingAPI.getQuestingUUID(mc.thePlayer);
        
        int n = 0;
        for(DBEntry<IQuestLine> iQuestLineDBEntry : lineList)
        {
            IQuestLine ql = iQuestLineDBEntry.getValue();
            EnumQuestVisibility vis = ql.getProperty(NativeProps.VISIBILITY);
            if(!canEdit && vis == EnumQuestVisibility.HIDDEN) continue;
        
            boolean show = false;
            boolean unlocked = false;
            boolean complete = false;
            boolean allComplete = true;
            boolean pendingClaim = false;
        
            if(canEdit)
            {
                show = true;
                unlocked = true;
                complete = true;
            }
            
            for(DBEntry<IQuestLineEntry> qID : ql.getEntries())
            {
                IQuest q = QuestDatabase.INSTANCE.getValue(qID.getID());
                if(q == null) continue;
                
                if(allComplete && !q.isComplete(playerID)) allComplete = false;
                if(!pendingClaim && q.isComplete(playerID) && !q.hasClaimed(playerID)) pendingClaim = true;
                if(!unlocked && q.isUnlocked(playerID)) unlocked = true;
                if(!complete && q.isComplete(playerID)) complete = true;
                if(!show && QuestCache.isQuestShown(q, playerID, mc.thePlayer)) show = true;
                if(unlocked && complete && show && pendingClaim && !allComplete) break;
            }
        
            if(vis == EnumQuestVisibility.COMPLETED && !complete)
            {
                continue;
            } else if(vis == EnumQuestVisibility.UNLOCKED && !unlocked)
            {
                continue;
            }
    
            cvLines.addPanel(new PanelGeneric(new GuiRectangle(126, n * 16, 16, 16, 0), new OreDictTexture(1F, ql.getProperty(NativeProps.ICON), false, true)));
            
            if(pendingClaim)
            {
                cvLines.addPanel(new PanelGeneric(new GuiRectangle(134, n * 16 + 8, 8, 8, -1), new GuiTextureColored(PresetIcon.ICON_NOTICE.getTexture(), new GuiColorStatic(0xFFFFFF00))));
            } else if(allComplete)
            {
                cvLines.addPanel(new PanelGeneric(new GuiRectangle(134, n * 16 + 8, 8, 8, -1), new GuiTextureColored(PresetIcon.ICON_TICK.getTexture(), new GuiColorStatic(0xFF00FF00))));
            }
            PanelButtonStorage<IQuestLine> btnLine = new PanelButtonStorage<>(new GuiRectangle(0, n++ * 16, 126, 16, 0), 1, QuestTranslation.translate(ql.getUnlocalisedName()), ql);
        
            if(!show || ql == selectedLine)
            {
                btnLine.setActive(false);
            }
        
            cvLines.addPanel(btnLine);
            qlBtns.add(btnLine);
        }
        
        scLines.setEnabled(cvLines.getScrollBounds().getHeight() > 0);
    }
    
    private void refreshContent()
    {
        if(selectedLineId >= 0)
        {
            selectedLine = QuestLineDatabase.INSTANCE.getValue(selectedLineId);
            if(selectedLine == null) selectedLineId = -1;
        } else
        {
            selectedLine = null;
        }
        
        cvQuest.setQuestLine(selectedLine);
        
        if(selectedLine != null)
        {
            paDesc.setText(QuestTranslation.translate(selectedLine.getUnlocalisedDescription()));
        } else
        {
            paDesc.setText("");
        }
        
        cvDesc.refreshScrollBounds();
        refreshClaimAll();
        
        scDesc.setEnabled(cvDesc.getScrollBounds().getHeight() > 0);
    }
    
    private void refreshClaimAll()
    {
        if(cvQuest.getQuestLine() == null || cvQuest.getQuestButtons().size() <= 0)
        {
            claimAll.setActive(false);
            return;
        }
        
        for(PanelButtonQuest btn : cvQuest.getQuestButtons())
        {
            if(btn.getStoredValue().getValue().canClaim(mc.thePlayer))
            {
                claimAll.setActive(true);
                return;
            }
        }
        
        claimAll.setActive(false);
    }
}
