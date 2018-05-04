package betterquesting.client.gui2.party;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelPlayerPortrait;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.UUID;

public class GuiPartyCreate extends GuiScreenCanvas implements IPEventListener, INeedsRefresh
{
    private PanelTextField flName;
    
    public GuiPartyCreate(GuiScreen parent)
    {
        super(parent);
    }
    
    @Override
    public void refreshGui()
    {
        this.initPanel();
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        UUID playerID = QuestingAPI.getQuestingUUID(mc.player);
        
        IParty curParty = PartyManager.INSTANCE.getUserParty(playerID);
        
        if(curParty != null)
        {
            mc.displayGuiScreen(new GuiPartyManage(parent));
            return;
        }
    
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
		Keyboard.enableRepeatEvents(true);
    
        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
    
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.back")));
    
        PanelTextBox txTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.party_none")).setAlignment(1);
        txTitle.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(txTitle);
    
        CanvasEmpty cvLeftHalf = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(16, 32, 8, 32), 0));
        cvBackground.addPanel(cvLeftHalf);
    
        PanelPlayerPortrait pnPortrait = new PanelPlayerPortrait(new GuiTransform(GuiAlign.TOP_CENTER, -32, 0, 64, 64, 0), mc.player);
        cvLeftHalf.addPanel(pnPortrait);
        
        cvLeftHalf.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_CENTER, 16, 48, 24, 24, 0), new ItemTexture(new BigItemStack(BetterQuesting.extraLife, LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(mc.player))), true, true)));
        
        PanelTextBox txName = new PanelTextBox(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(16, -44, 16, 28), 0), QuestTranslation.translate("betterquesting.gui.name"));
        txName.setColor(PresetColor.TEXT_HEADER.getColor());
        cvLeftHalf.addPanel(txName);
        
        flName = new PanelTextField(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(16, -32, 16, 16), 0), "New Party");
        cvLeftHalf.addPanel(flName);
        
        PanelButton btnCreate = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(16, -16, 16, 0), 0), 1, QuestTranslation.translate("betterquesting.btn.party_new"));
        cvLeftHalf.addPanel(btnCreate);
        
        CanvasEmpty cvRightHalf = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 32, 16, 32), 0));
        cvBackground.addPanel(cvRightHalf);
    
        CanvasScrolling cvInviteList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 8, 0), 0));
        cvRightHalf.addPanel(cvInviteList);
    
        PanelVScrollBar scInvite = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 16, 0, 0), 0));
        cvRightHalf.addPanel(scInvite);
        cvInviteList.setScrollDriverY(scInvite);
    
        PanelTextBox txInvite = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("betterquesting.gui.party_invites")).setAlignment(1);
        txInvite.setColor(PresetColor.TEXT_HEADER.getColor());
        cvRightHalf.addPanel(txInvite);
        
        int cvWidth = cvInviteList.getTransform().getWidth();
        List<Integer> invites = PartyManager.INSTANCE.getPartyInvites(playerID);
        int elSize = mc.fontRenderer.getStringWidth("...");
        
        for(int i = 0; i < invites.size(); i++)
        {
            Integer pid = invites.get(i);
            IParty party = PartyManager.INSTANCE.getValue(pid);
            
            if(party == null)
            {
                continue;
            }
    
            PanelButtonStorage<Integer> btnJoin = new PanelButtonStorage<>(new GuiRectangle(cvWidth - 50, i * 16, 50, 16, 0), 2, QuestTranslation.translate("betterquesting.btn.party_join"), pid);
            cvInviteList.addPanel(btnJoin);
            
            String pName = party.getName();
            if(mc.fontRenderer.getStringWidth(pName) > cvWidth - 58)
            {
                pName = mc.fontRenderer.trimStringToWidth(pName, cvWidth - 58 - elSize) + "...";
            }
            
            PanelTextBox txPartyName = new PanelTextBox(new GuiRectangle(0, i * 16 + 4, cvWidth - 58, 12, 0), pName);
            cvInviteList.addPanel(txPartyName);
        }
        
        scInvite.setActive(cvInviteList.getScrollBounds().getHeight() > 0);
        
        // Divider
    
        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 32, 0, 0, 0);
        ls0.setParent(cvBackground.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -32, 0, 0, 0);
        le0.setParent(cvBackground.getTransform());
        PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvBackground.addPanel(paLine0);
    }
    
    @Override
    public void onPanelEvent(PanelEvent event)
    {
        if(event instanceof PEventButton)
        {
            onButtonPress((PEventButton)event);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void onButtonPress(PEventButton event)
    {
        IPanelButton btn = event.getButton();
    
        if(btn.getButtonID() == 0) // Exit
        {
            mc.displayGuiScreen(this.parent);
        } else if(btn.getButtonID() == 1) // Create
        {
            NBTTagCompound tags = new NBTTagCompound();
            tags.setInteger("action", EnumPacketAction.ADD.ordinal());
            tags.setString("name", flName.getText());
            PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.PARTY_EDIT.GetLocation(), tags));
        } else if(btn.getButtonID() == 2 && btn instanceof PanelButtonStorage) // Join
        {
            NBTTagCompound tags = new NBTTagCompound();
            tags.setInteger("action", EnumPacketAction.JOIN.ordinal());
            tags.setInteger("partyID", ((PanelButtonStorage<Integer>)btn).getStoredValue());
            PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.PARTY_EDIT.GetLocation(), tags));
        }
    }
}