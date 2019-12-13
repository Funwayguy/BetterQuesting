package betterquesting.client.gui2.party;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.network.handlers.NetPartyAction;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: Make this use a proper scrolling search for big servers
public class GuiPartyInvite extends GuiScreenCanvas implements IPEventListener
{
    private IParty party;
    private int partyID;
    private PanelTextField<String> flName;
    
    public GuiPartyInvite(Screen parent)
    {
        super(parent);
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        UUID playerID = QuestingAPI.getQuestingUUID(minecraft.player);
        DBEntry<IParty> tmp = PartyManager.INSTANCE.getParty(playerID);
        
        if(tmp == null)
        {
            minecraft.displayGuiScreen(parent);
            return;
        }
        
        party = tmp.getValue();
        partyID = tmp.getID();
        
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
    
        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
    
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.back")));
    
        PanelTextBox txTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.party_invite", party.getProperties().getProperty(NativeProps.NAME))).setAlignment(1);
        txTitle.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(txTitle);
        
        flName = new PanelTextField<>(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(32, 32, 72, -48), 0), "", FieldFilterString.INSTANCE);
        flName.setMaxLength(16);
        flName.setWatermark("Username");
        cvBackground.addPanel(flName);
        
        PanelButton btnInvite = new PanelButton(new GuiTransform(GuiAlign.TOP_RIGHT, new GuiPadding(-72, 32, 32, -48), 0), 1, QuestTranslation.translate("betterquesting.btn.party_invite"));
        cvBackground.addPanel(btnInvite);
    
        CanvasScrolling cvNameList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(32, 64, 40, 32), 0));
        cvBackground.addPanel(cvNameList);
        
        PanelVScrollBar scNameScroll = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(0, 0, -8, 0), 0));
        cvBackground.addPanel(scNameScroll);
        scNameScroll.getTransform().setParent(cvNameList.getTransform());
        cvNameList.setScrollDriverY(scNameScroll);
        
        int listWidth = cvBackground.getTransform().getWidth() - 64;
        int nameSize = RenderUtils.getStringWidth("________________", font);
        int columnNum = listWidth/nameSize;
        
        List<String> nameList = new ArrayList<>();
        minecraft.player.connection.getPlayerInfoMap().forEach((info) -> nameList.add(info.getGameProfile().getName()));
        for(NetworkPlayerInfo info : minecraft.player.connection.getPlayerInfoMap())
        {
            if(!nameList.contains(info.getGameProfile().getName()))
            {
                nameList.add(info.getGameProfile().getName());
            }
        }
        
        nameList.removeIf((entry) -> {
           UUID memID = NameCache.INSTANCE.getUUID(entry);
           return party.getStatus(memID) != null;
        });
        
        for(int i = 0; i < nameList.size(); i++)
        {
            int x1 = i % columnNum;
            int y1 = i / columnNum;
            String name = nameList.get(i);
            PanelButtonStorage<String> btnName = new PanelButtonStorage<>(new GuiRectangle(x1 * nameSize, y1 * 16, nameSize, 16), 2, name, name);
            cvNameList.addPanel(btnName);
        }
        
        scNameScroll.setActive(cvNameList.getScrollBounds().getHeight() > 0);
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
            minecraft.displayGuiScreen(this.parent);
        } else if(btn.getButtonID() == 1 && flName.getRawText().length() > 0) // Manual Invite
        {
			CompoundNBT payload = new CompoundNBT();
			payload.putInt("action", 3);
			payload.putInt("partyID", partyID);
			payload.putString("username", flName.getRawText());
			payload.putLong("expiry", System.currentTimeMillis() + 60000L);
            NetPartyAction.sendAction(payload);
        } else if(btn.getButtonID() == 2 && btn instanceof PanelButtonStorage) // Invite
        {
            CompoundNBT payload = new CompoundNBT();
            payload.putInt("action", 3);
            payload.putInt("partyID", partyID);
            payload.putString("username", ((PanelButtonStorage<String>)btn).getStoredValue());
			payload.putLong("expiry", System.currentTimeMillis() + 60000L);
            NetPartyAction.sendAction(payload);
        }
    }
}
