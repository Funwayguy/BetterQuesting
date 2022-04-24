package betterquesting.client.gui2.party;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.controls.io.ValueFuncIO;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelHBarFill;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelPlayerPortrait;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.colors.GuiColorPulse;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.GuiColorTransition;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import betterquesting.network.handlers.NetPartyAction;
import betterquesting.questing.party.PartyInvitations;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class GuiPartyCreate extends GuiScreenCanvas implements IPEventListener, INeedsRefresh {
    private PanelTextField<String> flName;
    private CanvasScrolling invitePanel;
    private PanelVScrollBar inviteScroll;
    private UUID playerID;

    public GuiPartyCreate(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void refreshGui() {
        DBEntry<IParty> curParty = PartyManager.INSTANCE.getParty(playerID);

        if (curParty != null) {
            mc.displayGuiScreen(new GuiPartyManage(parent));
            return;
        }

        refreshInvites();
    }

    @Override
    public void initPanel() {
        super.initPanel();

        playerID = QuestingAPI.getQuestingUUID(mc.player);

        DBEntry<IParty> curParty = PartyManager.INSTANCE.getParty(playerID);

        if (curParty != null) {
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

        PanelPlayerPortrait pnPortrait = new PanelPlayerPortrait(new GuiTransform(GuiAlign.TOP_CENTER, -32, 0, 64, 64, 0), mc.player).setDepth(-16F);
        cvLeftHalf.addPanel(pnPortrait);

        cvLeftHalf.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_CENTER, 16, 48, 24, 24, 0), new ItemTexture(new BigItemStack(BetterQuesting.extraLife, LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(mc.player))), true, true).setDepth(32F)));

        PanelTextBox txName = new PanelTextBox(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(16, -44, 16, 28), 0), QuestTranslation.translate("betterquesting.gui.name"));
        txName.setColor(PresetColor.TEXT_HEADER.getColor());
        cvLeftHalf.addPanel(txName);

        flName = new PanelTextField<>(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(16, -32, 16, 16), 0), "New Party", FieldFilterString.INSTANCE);
        cvLeftHalf.addPanel(flName);

        PanelButton btnCreate = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(16, -16, 16, 0), 0), 1, QuestTranslation.translate("betterquesting.btn.party_new"));
        cvLeftHalf.addPanel(btnCreate);

        CanvasEmpty cvRightHalf = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 32, 16, 32), 0));
        cvBackground.addPanel(cvRightHalf);

        invitePanel = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 8, 0), 0));
        cvRightHalf.addPanel(invitePanel);

        inviteScroll = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 16, 0, 0), 0));
        cvRightHalf.addPanel(inviteScroll);
        invitePanel.setScrollDriverY(inviteScroll);

        PanelTextBox txInvite = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("betterquesting.gui.party_invites")).setAlignment(1);
        txInvite.setColor(PresetColor.TEXT_HEADER.getColor());
        cvRightHalf.addPanel(txInvite);

        refreshInvites();

        // Divider

        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 32, 0, 0, 0);
        ls0.setParent(cvBackground.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -32, 0, 0, 0);
        le0.setParent(cvBackground.getTransform());
        PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvBackground.addPanel(paLine0);
    }

    @Override
    public void onPanelEvent(PanelEvent event) {
        if (event instanceof PEventButton) {
            onButtonPress((PEventButton) event);
        }
    }

    @SuppressWarnings("unchecked")
    private void onButtonPress(PEventButton event) {
        IPanelButton btn = event.getButton();

        if (btn.getButtonID() == 0) // Exit
        {
            mc.displayGuiScreen(this.parent);
        } else if (btn.getButtonID() == 1) // Create
        {
            NBTTagCompound payload = new NBTTagCompound();
            payload.setInteger("action", 0);
            payload.setString("name", flName.getRawText());
            NetPartyAction.sendAction(payload);
        } else if (btn.getButtonID() == 2 && btn instanceof PanelButtonStorage) // Join
        {
            NBTTagCompound payload = new NBTTagCompound();
            payload.setInteger("action", 4);
            payload.setInteger("partyID", ((PanelButtonStorage<Integer>) btn).getStoredValue());
            NetPartyAction.sendAction(payload);
        }
    }

    private void refreshInvites() {
        invitePanel.resetCanvas();
        int cvWidth = invitePanel.getTransform().getWidth();
        List<Entry<Integer, Long>> invites = PartyInvitations.INSTANCE.getPartyInvites(playerID);
        int elSize = RenderUtils.getStringWidth("...", mc.fontRenderer);

        for (int i = 0; i < invites.size(); i++) {
            int pid = invites.get(i).getKey();
            if (pid < 0) continue;
            long exp = invites.get(i).getValue();
            IParty party = PartyManager.INSTANCE.getValue(pid);

            PanelButtonStorage<Integer> btnJoin = new PanelButtonStorage<>(new GuiRectangle(cvWidth - 50, i * 24, 50, 16, 0), 2, QuestTranslation.translate("betterquesting.btn.party_join"), pid);
            invitePanel.addPanel(btnJoin);

            String pName = party == null ? "Unknown (" + pid + ")" : party.getProperties().getProperty(NativeProps.NAME);
            if (RenderUtils.getStringWidth(pName, mc.fontRenderer) > cvWidth - 58) {
                pName = mc.fontRenderer.trimStringToWidth(pName, cvWidth - 58 - elSize) + "...";
            }

            PanelTextBox txPartyName = new PanelTextBox(new GuiRectangle(0, i * 24 + 4, cvWidth - 58, 12, 0), pName);
            txPartyName.setColor(PresetColor.TEXT_MAIN.getColor());
            invitePanel.addPanel(txPartyName);

            PanelHBarFill flExpiry = new PanelHBarFill(new GuiRectangle(0, i * 24 + 16, cvWidth, 8));
            ValueFuncIO<Float> expFunc = new ValueFuncIO<>(() -> (float) ((exp - System.currentTimeMillis()) / 300000D));
            flExpiry.setFillDriver(expFunc);
            flExpiry.setFillColor(new GuiColorTransition(new GuiColorStatic(0xFF00FF00), new GuiColorPulse(0xFFFF0000, 0xFFC00000, 1F, 0F)).setupBlending(false, 0.2F).setBlendDriver(expFunc));
            invitePanel.addPanel(flExpiry);
        }

        inviteScroll.setActive(invitePanel.getScrollBounds().getHeight() > 0);
    }
}