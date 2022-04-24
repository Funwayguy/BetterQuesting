package betterquesting.client.gui3;

import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.lists.CanvasHoverTray;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.util.vector.Vector4f;

public class GuiStatus extends GuiScreenCanvas {
    public GuiStatus(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void initPanel() {
        super.initPanel();

        // === BACKGROUND PANEL ===
        CanvasTextured bgCan = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(bgCan);

        CanvasHoverTray cvHover = new CanvasHoverTray(new GuiTransform(GuiAlign.MID_CENTER, 16, 16, 16, 16, 0), new GuiTransform(GuiAlign.MID_CENTER, 16, 16, 64, 64, 5), PresetTexture.PANEL_DARK.getTexture());
        bgCan.addPanel(cvHover);
        cvHover.getCanvasClosed().addPanel(new PanelGeneric(new GuiTransform(), PresetIcon.ICON_CROSS.getTexture()));
        cvHover.getCanvasOpen().addPanel(new PanelGeneric(new GuiTransform(), PresetIcon.ICON_TICK.getTexture()));

        // === SIDE BAR ===

        CanvasTextured bgSideBar = new CanvasTextured(new GuiTransform(new Vector4f(0F, 0F, 0.2F, 1F), new GuiPadding(8, 24, 4, 24), 0), PresetTexture.PANEL_INNER.getTexture());
        bgCan.addPanel(bgSideBar);

        CanvasScrolling cvBtnList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 8, 0), 0));
        bgSideBar.addPanel(cvBtnList);

        PanelVScrollBar scBtnList = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 0, 0, 0), 0));
        bgSideBar.addPanel(scBtnList);
        cvBtnList.setScrollDriverY(scBtnList);

        int lw = cvBtnList.getTransform().getWidth();
        int i = 0;

        PanelButton btnEntry = new PanelButton(new GuiRectangle(0, i++ * 24, lw, 16, 0), 0, "Status");
        cvBtnList.addPanel(btnEntry);

        btnEntry = new PanelButton(new GuiRectangle(0, i++ * 24, lw, 16, 0), 0, "Equip");
        cvBtnList.addPanel(btnEntry);

        btnEntry = new PanelButton(new GuiRectangle(0, i++ * 24, lw, 16, 0), 0, "Quests");
        cvBtnList.addPanel(btnEntry);

        btnEntry = new PanelButton(new GuiRectangle(0, i++ * 24, lw, 16, 0), 0, "Guild");
        cvBtnList.addPanel(btnEntry);

        btnEntry = new PanelButton(new GuiRectangle(0, i++ * 24, lw, 16, 0), 0, "Trade");
        cvBtnList.addPanel(btnEntry);

        // === QUICK BAR === (Settings, Party, Inbox, Theme)

        CanvasTextured bgQuickBar = new CanvasTextured(new GuiTransform(new Vector4f(0F, 1F, 0.2F, 1F), new GuiPadding(8, -24, 4, 8), 0), PresetTexture.PANEL_INNER.getTexture());
        bgCan.addPanel(bgQuickBar);

        PanelButton btnSettings = new PanelButton(new GuiTransform(new Vector4f(0F, 0F, 0.25F, 1F), new GuiPadding(0, 0, 0, 0), 0), 0, "");
        btnSettings.setIcon(PresetIcon.ICON_GEAR.getTexture());
        bgQuickBar.addPanel(btnSettings);

        PanelButton btnParty = new PanelButton(new GuiTransform(new Vector4f(0.25F, 0F, 0.5F, 1F), new GuiPadding(0, 0, 0, 0), 0), 0, "");
        btnParty.setIcon(PresetIcon.ICON_PARTY.getTexture());
        bgQuickBar.addPanel(btnParty);

        PanelButton btnInbox = new PanelButton(new GuiTransform(new Vector4f(0.5F, 0F, 0.75F, 1F), new GuiPadding(0, 0, 0, 0), 0), 0, "");
        btnInbox.setIcon(PresetIcon.ICON_NOTICE.getTexture());
        bgQuickBar.addPanel(btnInbox);

        PanelButton btnTheme = new PanelButton(new GuiTransform(new Vector4f(0.75F, 0F, 1F, 1F), new GuiPadding(0, 0, 0, 0), 0), 0, "");
        btnTheme.setIcon(PresetIcon.ICON_THEME.getTexture());
        bgQuickBar.addPanel(btnTheme);

        // === OUTER CHARACTER PANEL ===

        //CanvasTextured bgChar = new CanvasTextured(new GuiTransform(new Vector4f(0.2F, 0F, 1F, 1F), new GuiPadding(4, 24, 8, 48), 0), PresetTexture.PANEL_INNER.getTexture());
        //bgCan.addPanel(bgChar);

        // === CHARACTER PREVIEW ===

        //PanelEntityPreview pvChar = new PanelEntityPreview(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(0, 0, 4, 8), 0), mc.player);
        //bgChar.addPanel(pvChar);
        //pvChar.setRotationFixed(-15F, 0F);

        // === EQUIPMENT SLOTS ===

        // === LEVEL + XP BAR ===

        // === STAT LIST ===
    }
}
