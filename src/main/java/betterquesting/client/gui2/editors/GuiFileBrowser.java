package betterquesting.client.gui2.editors;

import betterquesting.api.misc.ICallback;
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
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasFileDirectory;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiFileBrowser extends GuiScreenCanvas implements IPEventListener {
    private final ICallback<File[]> callback;
    private final FileFilter filter;
    private final List<File> selList = new ArrayList<>();
    private PanelTextBox txtTitle;
    private File curDirectory;

    private CanvasScrolling cvSelected;
    private CanvasFileDirectory cvDirectory;
    private boolean multiSelect = true;

    public GuiFileBrowser(GuiScreen parent, ICallback<File[]> callback, File directory, @Nullable FileFilter filter) {
        super(parent);
        this.callback = callback;
        this.curDirectory = directory == null ? null : directory.getAbsoluteFile();
        this.filter = filter;
    }

    public GuiFileBrowser allowMultiSelect(boolean enable) {
        multiSelect = enable;
        return this;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        Keyboard.enableRepeatEvents(true);

        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        txtTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), curDirectory == null ? "*" : curDirectory.getAbsolutePath()).setAlignment(1);
        txtTitle.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(txtTitle);

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.done")));

        // === LEFT SIDE ===

        CanvasEmpty cvLeft = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(16, 32, 8, 24), 0));
        cvBackground.addPanel(cvLeft);

        PanelTextBox txtQuest = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("betterquesting.gui.selection")).setAlignment(1);
        txtQuest.setColor(PresetColor.TEXT_HEADER.getColor());
        cvLeft.addPanel(txtQuest);

        cvSelected = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 8, 0), 0));
        cvLeft.addPanel(cvSelected);

        PanelVScrollBar scReq = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 16, 0, 0), 0));
        cvLeft.addPanel(scReq);
        cvSelected.setScrollDriverY(scReq);

        // === RIGHT SIDE ===

        CanvasEmpty cvRight = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 32, 16, 24), 0));
        cvBackground.addPanel(cvRight);

        PanelTextBox txtDb = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("betterquesting.gui.folder")).setAlignment(1);
        txtDb.setColor(PresetColor.TEXT_HEADER.getColor());
        cvRight.addPanel(txtDb);

        PanelTextField<String> searchBox = new PanelTextField<>(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 16, 24, -32), 0), "", FieldFilterString.INSTANCE);
        searchBox.setWatermark("Search...");
        cvRight.addPanel(searchBox);

        cvDirectory = new CanvasFileDirectory(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 32, 8, 0), 0), curDirectory, filter) {
            @Override
            protected boolean addResult(File entry, int index, int width) {
                if (!entry.isDirectory()) {
                    PanelButtonStorage<File> btnAdd = new PanelButtonStorage<>(new GuiRectangle(0, index * 16, 16, 16, 0), -1, "", entry);
                    btnAdd.setIcon(PresetIcon.ICON_POSITIVE.getTexture());
                    btnAdd.setActive(!selList.contains(entry));
                    btnAdd.setCallback(value -> {
                        if (!multiSelect) selList.clear();
                        selList.add(value);
                        refreshSelected();
                        refreshSearch();
                    });
                    this.addPanel(btnAdd);
                } else {
                    // Keeps the scrolling region's left side from auto-cropping when no files are present to select.
                    PanelGeneric pnDummy = new PanelGeneric(new GuiRectangle(0, index * 16, 16, 16, 0), null);
                    this.addPanel(pnDummy);
                }

                PanelButtonStorage<File> btnEdit = new PanelButtonStorage<>(new GuiRectangle(16, index * 16, width - 32, 16, 0), -1, curDirectory == null ? entry.getAbsolutePath() : entry.getName(), entry);
                btnEdit.setActive(entry.isDirectory());
                btnEdit.setCallback(value -> {
                    curDirectory = value;
                    this.setCurDirectory(curDirectory);
                    txtTitle.setText(curDirectory == null ? "*" : curDirectory.getAbsolutePath());
                });
                this.addPanel(btnEdit);

                PanelGeneric pnIco = new PanelGeneric(new GuiRectangle(width - 16, index * 16, 16, 16, 0), entry.isDirectory() ? PresetIcon.ICON_FOLDER_OPEN.getTexture() : PresetIcon.ICON_FILE.getTexture());
                this.addPanel(pnIco);

                return true;
            }
        };
        cvRight.addPanel(cvDirectory);

        PanelButton selAll = new PanelButton(new GuiTransform(GuiAlign.TOP_RIGHT, -24, 16, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                if (!multiSelect) return;
                boolean changed = false;

                for (File file : cvDirectory.getResults()) {
                    if (!file.isDirectory() && !selList.contains(file)) {
                        selList.add(file);
                        changed = true;
                    }
                }

                if (changed) {
                    cvDirectory.refreshSearch();
                    refreshSelected();
                }
            }
        };
        selAll.setActive(multiSelect);
        selAll.setTooltip(Collections.singletonList("Select All"));
        selAll.setIcon(PresetIcon.ICON_SELECTION.getTexture());
        cvRight.addPanel(selAll);

        searchBox.setCallback(cvDirectory::setSearchFilter);

        PanelVScrollBar scDb = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 32, 0, 0), 0));
        cvRight.addPanel(scDb);
        cvDirectory.setScrollDriverY(scDb);

        PanelButton btnNew = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, 0, 16, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                if (curDirectory == null) return;
                curDirectory = curDirectory.getParentFile();
                cvDirectory.setCurDirectory(curDirectory);
                txtTitle.setText(curDirectory == null ? "*" : curDirectory.getAbsolutePath());
            }
        };
        btnNew.setIcon(PresetIcon.ICON_DIR_UP.getTexture());
        cvRight.addPanel(btnNew);

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
            if (callback != null) callback.setValue(selList.toArray(new File[0]));
            mc.displayGuiScreen(this.parent);
        }
    }

    private void refreshSelected() {
        cvSelected.resetCanvas();

        int width = cvSelected.getTransform().getWidth();
        for (int i = 0; i < selList.size(); i++) {
            File f = selList.get(i);
            PanelButton btnSel = new PanelButton(new GuiRectangle(0, i * 16, width - 16, 16, 0), -1, f.getName());
            btnSel.setActive(false);
            cvSelected.addPanel(btnSel);

            PanelButtonStorage<File> btnFile = new PanelButtonStorage<>(new GuiRectangle(width - 16, i * 16, 16, 16, 0), -1, "", f);
            btnFile.setIcon(PresetIcon.ICON_NEGATIVE.getTexture());
            btnFile.setCallback(value -> {
                selList.remove(value);
                refreshSelected();
                cvDirectory.refreshSearch();
            });
            cvSelected.addPanel(btnFile);
        }
    }
}
