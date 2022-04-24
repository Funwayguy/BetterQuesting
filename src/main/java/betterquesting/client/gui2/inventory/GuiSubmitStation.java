package betterquesting.client.gui2.inventory;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.IFluidTask;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.GuiContainerCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.blocks.TileSubmitStation;
import betterquesting.network.handlers.NetStationEdit;
import betterquesting.questing.QuestDatabase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.NonNullList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector4f;

import java.util.Iterator;
import java.util.List;

public class GuiSubmitStation extends GuiContainerCanvas implements INeedsRefresh {
    private final ContainerSubmitStation ssContainer;
    private final TileSubmitStation tile;

    private final NonNullList<DBEntry<IQuest>> quests = NonNullList.create();
    private final NonNullList<DBEntry<ITask>> tasks = NonNullList.create();

    private IGuiCanvas cvBackground;

    private IPanelButton btnSet;
    private IPanelButton btnRem;
    private IPanelButton btnQstLeft;
    private IPanelButton btnQstRight;
    private IPanelButton btnTskLeft;
    private IPanelButton btnTskRight;

    private PanelTextBox txtQstTitle;
    private PanelTextBox txtTskTitle;

    private IGuiPanel taskPanel;

    private int selQuest = 0;
    private int selTask = 0;

    public GuiSubmitStation(GuiScreen parent, InventoryPlayer playerInvo, TileSubmitStation submitStation) {
        super(parent, new ContainerSubmitStation(playerInvo, submitStation));
        this.ssContainer = (ContainerSubmitStation) this.inventorySlots;
        this.tile = submitStation;
    }

    @Override
    public void refreshGui() {
        quests.clear();
        QuestCache qc = mc.player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        if (qc != null) quests.addAll(QuestDatabase.INSTANCE.bulkLookup(qc.getActiveQuests()));
        filterQuests();

        refreshTaskPanel();
    }

    @Override
    public void initPanel() {
        super.initPanel();

        Keyboard.enableRepeatEvents(true);

        quests.clear();
        taskPanel = null;
        QuestCache qc = mc.player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        if (qc != null) quests.addAll(QuestDatabase.INSTANCE.bulkLookup(qc.getActiveQuests()));
        filterQuests();

        // Background panel
        cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        PanelTextBox txtTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.submit_station")).setAlignment(1);
        txtTitle.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(txtTitle);

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), -1, QuestTranslation.translate("gui.done")).setClickAction((b) -> mc.displayGuiScreen(parent)));

        btnQstLeft = new PanelButton(new GuiTransform(new Vector4f(0.5F, 0F, 0.5F, 0F), 8, 32, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                selQuest--;
                refreshTaskPanel();
            }
        };
        ((PanelButton) btnQstLeft).setIcon(PresetIcon.ICON_LEFT.getTexture());
        cvBackground.addPanel(btnQstLeft);

        btnQstRight = new PanelButton(new GuiTransform(new Vector4f(1F, 0F, 1F, 0F), -32, 32, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                selQuest++;
                refreshTaskPanel();
            }
        };
        ((PanelButton) btnQstRight).setIcon(PresetIcon.ICON_RIGHT.getTexture());
        cvBackground.addPanel(btnQstRight);

        btnTskLeft = new PanelButton(new GuiTransform(new Vector4f(0.5F, 0F, 0.5F, 0F), 8, 48, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                selTask--;
                refreshTaskPanel();
            }
        };
        ((PanelButton) btnTskLeft).setIcon(PresetIcon.ICON_LEFT.getTexture());
        cvBackground.addPanel(btnTskLeft);

        btnTskRight = new PanelButton(new GuiTransform(new Vector4f(1F, 0F, 1F, 0F), -32, 48, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                selTask++;
                refreshTaskPanel();
            }
        };
        ((PanelButton) btnTskRight).setIcon(PresetIcon.ICON_RIGHT.getTexture());
        cvBackground.addPanel(btnTskRight);

        btnSet = new PanelButton(new GuiTransform(new Vector4f(0.75F, 0F, 0.75F, 0F), -16, 64, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                tile.setupTask(QuestingAPI.getQuestingUUID(mc.player), quests.get(selQuest).getValue(), tasks.get(selTask).getValue());
                NetStationEdit.setupStation(tile.getPos(), selQuest, selTask);
                refreshTaskPanel();
            }
        };
        ((PanelButton) btnSet).setIcon(PresetIcon.ICON_TICK.getTexture(), new GuiColorStatic(0xFF00FF00), 0);
        cvBackground.addPanel(btnSet);

        btnRem = new PanelButton(new GuiTransform(new Vector4f(0.75F, 0F, 0.75F, 0F), 0, 64, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                tile.reset();
                NetStationEdit.resetStation(tile.getPos());
                refreshTaskPanel();
            }
        };
        ((PanelButton) btnRem).setIcon(PresetIcon.ICON_CROSS.getTexture(), new GuiColorStatic(0xFFFF0000), 0);
        cvBackground.addPanel(btnRem);

        txtQstTitle = new PanelTextBox(new GuiTransform(new Vector4f(0.5F, 0F, 1F, 0F), new GuiPadding(24, 36, 32, -48), 0), "");
        txtQstTitle.setColor(PresetColor.TEXT_MAIN.getColor()).setAlignment(1);
        cvBackground.addPanel(txtQstTitle);

        txtTskTitle = new PanelTextBox(new GuiTransform(new Vector4f(0.5F, 0F, 1F, 0F), new GuiPadding(24, 52, 32, -64), 0), "");
        txtTskTitle.setColor(PresetColor.TEXT_MAIN.getColor()).setAlignment(1);
        cvBackground.addPanel(txtTskTitle);

        setInventoryPosition((xSize - 64) / 4 + 16 - 81, (ySize - 64) / 2 + 16 - 49);
        refreshTaskPanel();
    }

    private void setInventoryPosition(int x, int y) {
        // OLD SCHOOL GUI MATH
        // Move the slots into place manually (coordinates are already relative to the parent transform)
        ssContainer.moveInventorySlots(x + 17, y + 39);
        ssContainer.moveSubmitSlot(x + 71, y + 17);
        ssContainer.moveReturnSlot(x + 107, y + 17);

        for (int i = 0; i < 36; i++) {
            int j = i % 9 * 18;
            int k = i / 9 * 18;
            if (i >= 27) k += 4;

            this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, x + j, y + k + 22, 18, 18, -1), PresetTexture.ITEM_FRAME.getTexture()));
        }

        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, x + 54, y, 18, 18, -1), PresetTexture.ITEM_FRAME.getTexture()));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, x + 72, y, 18, 18, -1), PresetIcon.ICON_RIGHT.getTexture()));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, x + 90, y, 18, 18, -1), PresetTexture.ITEM_FRAME.getTexture()));
    }

    private void filterQuests() {
        Iterator<DBEntry<IQuest>> iter = quests.iterator();

        while (iter.hasNext()) {
            DBEntry<IQuest> entry = iter.next();

            boolean valid = false;

            for (DBEntry<ITask> tsk : entry.getValue().getTasks().getEntries()) {
                if (tsk.getValue() instanceof IItemTask || tsk.getValue() instanceof IFluidTask) {
                    valid = true;
                    break;
                }
            }

            if (!valid) iter.remove();
        }
    }

    private void refreshTaskPanel() {
        if (taskPanel != null) cvBackground.removePanel(taskPanel);

        if (tile.isSetup()) {
            DBEntry<IQuest> qdbe = null;

            for (int i = 0; i < quests.size(); i++) {
                DBEntry<IQuest> entry = quests.get(i);
                if (entry.getID() == tile.questID) {
                    selQuest = i;
                    qdbe = entry;
                    break;
                }
            }

            if (qdbe != null) {
                List<DBEntry<ITask>> tmpTasks = qdbe.getValue().getTasks().getEntries();
                for (int i = 0; i < tmpTasks.size(); i++) {
                    if (tmpTasks.get(i).getID() == tile.taskID) {
                        selTask = i;
                        break;
                    }
                }
            }

            btnRem.setActive(true);
            btnSet.setActive(false);

            btnQstLeft.setActive(false);
            btnQstRight.setActive(false);
            btnTskLeft.setActive(false);
            btnTskRight.setActive(false);
        } else {
            btnRem.setActive(false);
            btnSet.setActive(false);

            btnQstLeft.setActive(true);
            btnQstRight.setActive(true);
            btnTskLeft.setActive(true);
            btnTskRight.setActive(true);
        }

        if (quests.size() <= 0) {
            selQuest = -1;
            selTask = -1;
            txtQstTitle.setText("");
            txtTskTitle.setText("");
            return;
        } else selQuest = lazyPosMod(selQuest, quests.size());

        DBEntry<IQuest> entry = quests.get(selQuest);
        txtQstTitle.setText(QuestTranslation.translate(entry.getValue().getProperty(NativeProps.NAME)));

        tasks.clear();
        tasks.addAll(entry.getValue().getTasks().getEntries());

        if (tasks.size() <= 0) {
            selTask = -1;
            txtTskTitle.setText("");
            return;
        } else selTask = lazyPosMod(selTask, tasks.size());

        DBEntry<ITask> curTask = tasks.get(selTask);
        txtTskTitle.setText(QuestTranslation.translate(curTask.getValue().getUnlocalisedName()));
        btnSet.setActive(!tile.isSetup() && (curTask.getValue() instanceof IItemTask || curTask.getValue() instanceof IFluidTask));

        taskPanel = curTask.getValue().getTaskGui(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 88, 16, 24), 0), entry);
        if (taskPanel != null) cvBackground.addPanel(taskPanel);
    }

    private int lazyPosMod(int a, int b) {
        return ((a % b) + b) % b;
    }
}
