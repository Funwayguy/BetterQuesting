package betterquesting.client.gui2;

import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestSearch;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.misc.QuestSearchEntry;
import net.minecraft.client.gui.GuiScreen;

import java.util.function.Consumer;

public class GuiQuestSearch extends GuiScreenCanvas {

    private PanelTextField<String> searchBox;

    public GuiQuestSearch(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void initPanel() {
        super.initPanel();
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        CanvasEmpty cvInner = new CanvasEmpty(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 8, 8), 0));
        cvBackground.addPanel(cvInner);

        createExitButton(cvInner);

        PanelTextBox txtDb = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("betterquesting.gui.search")).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor());
        cvInner.addPanel(txtDb);

        createSearchBox(cvInner);
    }

    private void createExitButton(CanvasEmpty cvInner) {
        PanelButton btnExit = new PanelButton(
                new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0),
                0,
                QuestTranslation.translate("gui.back")
        );
        btnExit.setClickAction((b) -> mc.displayGuiScreen(parent));
        cvInner.addPanel(btnExit);
    }

    private void createSearchBox(CanvasEmpty cvInner) {
        searchBox = new PanelTextField<>(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 8, -32), 0), "", FieldFilterString.INSTANCE);
        searchBox.setWatermark("Search...");
        searchBox.lockFocus(true);
        cvInner.addPanel(searchBox);

        CanvasQuestSearch canvasQuestSearch = createSearchCanvas();
        cvInner.addPanel(canvasQuestSearch);

        searchBox.setCallback(canvasQuestSearch::setSearchFilter);

        PanelVScrollBar scDb = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 32, 0, 24), 0));
        cvInner.addPanel(scDb);
        canvasQuestSearch.setScrollDriverY(scDb);
    }

    private CanvasQuestSearch createSearchCanvas(){

        CanvasQuestSearch canvasQuestSearch = new CanvasQuestSearch(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 32, 8, 24), 0), mc.player);
        canvasQuestSearch.setQuestOpenCallback(questSearchEntry -> {
            acceptCallback(questSearchEntry);
            GuiHome.bookmark = new GuiQuest(parent, questSearchEntry.getQuest().getID());
            mc.displayGuiScreen(GuiHome.bookmark);
        });
        canvasQuestSearch.setQuestHighlightCallback(questSearchEntry -> {
            mc.displayGuiScreen(parent);
            acceptCallback(questSearchEntry);
        });
        return canvasQuestSearch;
    }

    private Consumer<QuestSearchEntry> callback;

    public void setCallback(Consumer<QuestSearchEntry> callback) {
        this.callback = callback;
    }

    private void acceptCallback(QuestSearchEntry questSearchEntry){
        if (callback != null) callback.accept(questSearchEntry);
    }

    public boolean isSearchFocused() {
        return searchBox != null && searchBox.isFocused();
    }
}
