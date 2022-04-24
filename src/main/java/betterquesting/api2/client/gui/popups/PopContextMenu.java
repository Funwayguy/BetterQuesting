package betterquesting.api2.client.gui.popups;

import betterquesting.api2.client.gui.SceneController;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasResizeable;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PopContextMenu extends CanvasEmpty {
    private final ContextCategory catRoot = new ContextCategory(null, "root");
    private final GuiRectangle rect;
    private final boolean autoClose;

    public PopContextMenu(GuiRectangle rect, boolean autoClose) {
        super(rect);
        this.rect = rect;
        this.autoClose = autoClose;
    }

    public ContextCategory getRootCategory() {
        return this.catRoot;
    }

    public void addButton(@Nonnull String text, @Nullable IGuiTexture icon, @Nullable Runnable action) {
        catRoot.addButton(text, icon, action);
    }

    public ContextCategory addCateogry(@Nonnull String text) {
        return catRoot.addCateogry(text);
    }

    public void openCategory(@Nonnull ContextCategory category) {
        this.resetCanvas();

        int listH = Math.min(category.entries.size() * 16, rect.getHeight());

        if (getTransform().getParent() != null) // Auto adjust if hanging off screen
        {
            IGuiRect par = getTransform().getParent();
            rect.x += Math.min(0, (par.getX() + par.getWidth()) - (rect.getX() + rect.getWidth()));
            rect.y += Math.min(0, (par.getY() + par.getHeight()) - (rect.getY() + listH));
        }

        CanvasResizeable cvBG = new CanvasResizeable(new GuiRectangle(0, 0, 0, 0, 0), PresetTexture.PANEL_INNER.getTexture());
        this.addPanel(cvBG);
        cvBG.lerpToRect(new GuiRectangle(0, 0, rect.w - 8, listH, 0), 100, true);

        CanvasScrolling cvScroll = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX));
        cvBG.addPanel(cvScroll);

        PanelVScrollBar scrollBar = new PanelVScrollBar(new GuiRectangle(rect.w - 8, 0, 8, listH, 0));
        this.addPanel(scrollBar);
        cvScroll.setScrollDriverY(scrollBar);

        for (int i = 0; i < category.entries.size(); i++) {
            ContextEntry entry = category.entries.get(i);
            if (entry.icon != null) {
                cvScroll.addPanel(new PanelGeneric(new GuiRectangle(0, i * 16, 16, 16, 0), entry.icon));
                PanelButton eBtn = new PanelButton(new GuiRectangle(16, i * 16, rect.w - 24, 16, 0), -1, QuestTranslation.translate(entry.text));
                if (entry.action != null) {
                    eBtn.setClickAction((b) -> entry.action.run());
                } else {
                    eBtn.setActive(false);
                }
                cvScroll.addPanel(eBtn);
            } else {
                PanelButton eBtn = new PanelButton(new GuiRectangle(0, i * 16, rect.w - 8, 16, 0), -1, QuestTranslation.translate(entry.text));
                if (entry.action != null) {
                    eBtn.setClickAction((b) -> entry.action.run());
                } else {
                    eBtn.setActive(false);
                }
                cvScroll.addPanel(eBtn);
            }
        }

        scrollBar.setEnabled(cvScroll.getScrollBounds().getHeight() > 0);
    }

    @Override
    public void initPanel() {
        super.initPanel();

        openCategory(catRoot);
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        boolean used = super.onMouseClick(mx, my, click);

        if (autoClose && !used && !rect.contains(mx, my) && SceneController.getActiveScene() != null) {
            SceneController.getActiveScene().closePopup();
            return true;
        }

        return used;
    }

    public class ContextCategory {
        private final String name;
        private final ContextCategory parent;
        private final List<ContextEntry> entries = new ArrayList<>();

        private ContextCategory(@Nullable ContextCategory parent, @Nonnull String name) {
            this.parent = parent;
            this.name = name;

            if (this.parent != null) addButton("<", null, () -> openCategory(this.parent));
        }

        public void addButton(@Nonnull String text, @Nullable IGuiTexture icon, @Nullable Runnable action) {
            entries.add(new ContextEntry(text, icon, action));
        }

        public ContextCategory addCateogry(@Nonnull String text) {
            ContextCategory cat = new ContextCategory(this, text);
            addButton(QuestTranslation.translate(text) + " >", null, () -> openCategory(cat));
            return cat;
        }
    }

    public class ContextEntry {
        private final String text;
        private final IGuiTexture icon;
        private final Runnable action;

        public ContextEntry(@Nonnull String text, @Nullable IGuiTexture icon, @Nullable Runnable action) {
            this.text = text;
            this.icon = icon;
            this.action = action;
        }
    }
}
