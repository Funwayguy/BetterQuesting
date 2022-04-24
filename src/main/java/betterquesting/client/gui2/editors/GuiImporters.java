package betterquesting.client.gui2.editors;

import betterquesting.api.client.importers.IImporter;
import betterquesting.api.misc.ICallback;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.importers.ImportedQuestLines;
import betterquesting.client.importers.ImportedQuests;
import betterquesting.client.importers.ImporterRegistry;
import betterquesting.network.handlers.NetImport;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.List;

public class GuiImporters extends GuiScreenCanvas implements IPEventListener, ICallback<File[]> {
    private PanelTextBox impName;
    private CanvasScrolling impDescCV;
    private PanelTextBox impDescTX;
    private PanelButtonStorage<IImporter> impBtn;

    public GuiImporters(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void initPanel() {
        super.initPanel();

        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        Keyboard.enableRepeatEvents(true);

        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        PanelTextBox panTxt = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.importers")).setAlignment(1);
        panTxt.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(panTxt);

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.back")));

        // === LEFT SIDE ===

        CanvasScrolling cvImports = new CanvasScrolling(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(16, 32, 16, 24), 0));
        cvBackground.addPanel(cvImports);

        int width = cvImports.getTransform().getWidth();
        List<IImporter> impList = ImporterRegistry.INSTANCE.getImporters();

        for (int i = 0; i < impList.size(); i++) {
            IImporter imp = impList.get(i);
            cvImports.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, i * 16, width, 16, 0), 1, QuestTranslation.translate(imp.getUnlocalisedName()), imp));
        }

        PanelVScrollBar scReq = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(0, 0, -8, 0), 0));
        cvBackground.addPanel(scReq);
        scReq.getTransform().setParent(cvImports.getTransform());
        cvImports.setScrollDriverY(scReq);

        // === RIGHT SIDE ===

        CanvasEmpty cvRight = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 32, 16, 24), 0));
        cvBackground.addPanel(cvRight);

        impName = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), "").setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor());
        cvRight.addPanel(impName);

        impDescCV = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 8, 24), 0));
        cvRight.addPanel(impDescCV);

        PanelVScrollBar scDesc = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(0, 0, -8, 0), 0));
        cvRight.addPanel(scDesc);
        scDesc.getTransform().setParent(impDescCV.getTransform());
        impDescCV.setScrollDriverY(scDesc);

        width = impDescCV.getTransform().getWidth();
        impDescTX = new PanelTextBox(new GuiRectangle(0, 0, width, 16, 0), "", true).setColor(PresetColor.TEXT_MAIN.getColor());
        impDescCV.addPanel(impDescTX);

        impBtn = new PanelButtonStorage<>(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(0, -16, 0, 0), 0), 2, QuestTranslation.translate("betterquesting.btn.import"), null);
        impBtn.setActive(false);
        cvRight.addPanel(impBtn);

        // === DIVIDERS ===

        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 32, 0, 0, 0);
        ls0.setParent(cvBackground.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -24, 0, 0, 0);
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
        } else if (btn.getButtonID() == 1) // Select
        {
            IImporter imp = ((PanelButtonStorage<IImporter>) btn).getStoredValue();
            impName.setText(QuestTranslation.translate(imp.getUnlocalisedName()));
            impDescTX.setText(QuestTranslation.translate(imp.getUnlocalisedDescription()));
            impDescCV.refreshScrollBounds();
            impBtn.setStoredValue(imp);
            impBtn.setActive(true);
        } else if (btn.getButtonID() == 2) // Import
        {
            lastImport = ((PanelButtonStorage<IImporter>) btn).getStoredValue();
            mc.displayGuiScreen(new GuiFileBrowser(this, this, new File(".").getAbsoluteFile().getParentFile(), lastImport.getFileFilter()));
        }
    }

    private IImporter lastImport;

    @Override
    public void setValue(File[] files) {
        if (files == null || files.length <= 0 || lastImport == null) {
            return;
        }

        ImportedQuests questDB = new ImportedQuests();
        IQuestLineDatabase lineDB = new ImportedQuestLines();

        lastImport.loadFiles(questDB, lineDB, files);

        if (questDB.size() > 0 || lineDB.size() > 0) {
            NetImport.sendImport(questDB, lineDB);
            mc.displayGuiScreen(parent);
        }
    }
}
