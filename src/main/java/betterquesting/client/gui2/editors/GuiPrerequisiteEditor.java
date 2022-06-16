package betterquesting.client.gui2.editors;

import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
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
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestDatabase;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.gui2.GuiQuest;
import betterquesting.network.handlers.NetQuestEdit;
import betterquesting.questing.QuestDatabase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GuiPrerequisiteEditor extends GuiScreenCanvas implements IPEventListener, IVolatileScreen, INeedsRefresh {
    private IQuest quest;
    private final int questID;

    private CanvasQuestDatabase canvasDB;
    private CanvasScrolling canvasPreReq;

    public GuiPrerequisiteEditor(GuiScreen parent, IQuest quest) {
        super(parent);
        this.quest = quest;
        this.questID = QuestDatabase.INSTANCE.getID(quest);
    }

    @Override
    public void refreshGui() {
        quest = QuestDatabase.INSTANCE.getValue(questID);

        if (quest == null) {
            mc.displayGuiScreen(parent);
            return;
        }

        canvasDB.refreshSearch();
        refreshReqCanvas();
    }

    @Override
    public void initPanel() {
        super.initPanel();

        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        Keyboard.enableRepeatEvents(true);

        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        PanelTextBox panTxt = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.pre_requisites")).setAlignment(1);
        panTxt.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(panTxt);

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.back")));

        // === RIGHT SIDE ===

        CanvasEmpty cvRight = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 32, 16, 24), 0));
        cvBackground.addPanel(cvRight);

        PanelTextBox txtDb = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("betterquesting.gui.database")).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor());
        cvRight.addPanel(txtDb);

        PanelTextField<String> searchBox = new PanelTextField<>(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 8, -32), 0), "", FieldFilterString.INSTANCE);
        searchBox.setWatermark("Search...");
        cvRight.addPanel(searchBox);

        canvasDB = new CanvasQuestDatabase(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 32, 8, 24), 0)) {
            @Override
            protected boolean addResult(DBEntry<IQuest> entry, int index, int width) {
                PanelButtonStorage<DBEntry<IQuest>> btnAdd = new PanelButtonStorage<>(new GuiRectangle(0, index * 16, 16, 16, 0), 2, "", entry);
                btnAdd.setIcon(PresetIcon.ICON_POSITIVE.getTexture());
                btnAdd.setActive(!containsReq(quest, entry.getID()));
                this.addPanel(btnAdd);

                PanelButtonStorage<DBEntry<IQuest>> btnEdit = new PanelButtonStorage<>(new GuiRectangle(16, index * 16, width - 32, 16, 0), 1, QuestTranslation.translate(entry.getValue().getProperty(NativeProps.NAME)), entry);
                this.addPanel(btnEdit);

                PanelButtonStorage<DBEntry<IQuest>> btnDel = new PanelButtonStorage<>(new GuiRectangle(width - 16, index * 16, 16, 16, 0), 4, "", entry);
                btnDel.setIcon(PresetIcon.ICON_TRASH.getTexture());
                this.addPanel(btnDel);

                return true;
            }
        };
        cvRight.addPanel(canvasDB);

        searchBox.setCallback(canvasDB::setSearchFilter);

        PanelVScrollBar scDb = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 32, 0, 24), 0));
        cvRight.addPanel(scDb);
        canvasDB.setScrollDriverY(scDb);

        PanelButton btnNew = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(0, -16, 0, 0), 0), 5, QuestTranslation.translate("betterquesting.btn.new"));
        cvRight.addPanel(btnNew);

        // === LEFT SIDE ===

        CanvasEmpty cvLeft = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(16, 32, 8, 24), 0));
        cvBackground.addPanel(cvLeft);

        PanelTextBox txtQuest = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate(quest.getProperty(NativeProps.NAME))).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor());
        cvLeft.addPanel(txtQuest);

        canvasPreReq = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 8, 0), 0));
        cvLeft.addPanel(canvasPreReq);

        PanelVScrollBar scReq = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 16, 0, 0), 0));
        cvLeft.addPanel(scReq);
        canvasPreReq.setScrollDriverY(scReq);

        // === DIVIDERS ===

        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 32, 0, 0, 0);
        ls0.setParent(cvBackground.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -24, 0, 0, 0);
        le0.setParent(cvBackground.getTransform());
        PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvBackground.addPanel(paLine0);

        refreshReqCanvas();
    }

    private void refreshReqCanvas() {
        canvasPreReq.resetCanvas();
        int width = canvasPreReq.getTransform().getWidth();

        List<DBEntry<IQuest>> arrReq = QuestDatabase.INSTANCE.bulkLookup(quest.getRequirements());
        for (int i = 0; i < arrReq.size(); i++) {
            PanelButtonStorage<DBEntry<IQuest>> btnEdit = new PanelButtonStorage<>(new GuiRectangle(0, i * 16, width - 48, 16, 0), 1,
                    QuestTranslation.translate(arrReq.get(i).getValue().getProperty(NativeProps.NAME)), arrReq.get(i));
            canvasPreReq.addPanel(btnEdit);

            PanelButtonStorage<DBEntry<IQuest>> btnType = new PanelButtonStorage<>(new GuiRectangle(width - 48, i * 16, 16, 16, 0), 6, "", arrReq.get(i));
            int arrReqID = arrReq.get(i).getID();
            btnType.setIcon(quest.getRequirementType(arrReqID).getIcon().getTexture());
            if (quest.getRequirementType(arrReqID) == IQuest.RequirementType.NORMAL)
                btnType.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.btn.visible_always")));
            else if (quest.getRequirementType(arrReqID) == IQuest.RequirementType.IMPLICIT)
                btnType.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.btn.visible_implicit")));
            else
                btnType.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.btn.visible_hidden")));
            canvasPreReq.addPanel(btnType);

            PanelButtonStorage<DBEntry<IQuest>> btnUp = new PanelButtonStorage<>(new GuiRectangle(width - 32, i * 16, 16, 16, 0), 7, "", arrReq.get(i));
            btnUp.setIcon(PresetIcon.ICON_UP.getTexture());
            btnUp.setActive(arrReq.size() > 1);
            canvasPreReq.addPanel(btnUp);

            PanelButtonStorage<DBEntry<IQuest>> btnRem = new PanelButtonStorage<>(new GuiRectangle(width - 16, i * 16, 16, 16, 0), 3, "", arrReq.get(i));
            btnRem.setIcon(PresetIcon.ICON_NEGATIVE.getTexture());
            canvasPreReq.addPanel(btnRem);
        }
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

        if (btn.getButtonID() == 0) { // Exit
            mc.displayGuiScreen(this.parent);
        } else if (btn.getButtonID() == 1 && btn instanceof PanelButtonStorage) { // Edit Quest
            DBEntry<IQuest> entry = ((PanelButtonStorage<DBEntry<IQuest>>) btn).getStoredValue();
            mc.displayGuiScreen(new GuiQuest(this, entry.getID()));
        } else if (btn.getButtonID() == 2 && btn instanceof PanelButtonStorage) { // Add
            DBEntry<IQuest> entry = ((PanelButtonStorage<DBEntry<IQuest>>) btn).getStoredValue();
            addReq(quest, entry.getID());
            SendChanges();
        } else if (btn.getButtonID() == 3 && btn instanceof PanelButtonStorage) { // Remove
            DBEntry<IQuest> entry = ((PanelButtonStorage<DBEntry<IQuest>>) btn).getStoredValue();
            removeReq(quest, entry.getID());
            SendChanges();
        } else if (btn.getButtonID() == 4 && btn instanceof PanelButtonStorage) { // Delete
            DBEntry<IQuest> entry = ((PanelButtonStorage<DBEntry<IQuest>>) btn).getStoredValue();
            NBTTagCompound payload = new NBTTagCompound();
            payload.setIntArray("questIDs", new int[]{entry.getID()});
            payload.setInteger("action", 1);
            NetQuestEdit.sendEdit(payload);
        } else if (btn.getButtonID() == 5) { // New
            NBTTagCompound payload = new NBTTagCompound();
            NBTTagList dataList = new NBTTagList();
            NBTTagCompound entry = new NBTTagCompound();
            entry.setInteger("questID", -1);
            dataList.appendTag(entry);
            payload.setTag("data", dataList);
            NetQuestEdit.sendEdit(payload);
        } else if (btn.getButtonID() == 6) { // set type
            DBEntry<IQuest> entry = ((PanelButtonStorage<DBEntry<IQuest>>) btn).getStoredValue();
            quest.setRequirementType(entry.getID(), quest.getRequirementType(entry.getID()).next());
            SendChanges();
        } else if (btn.getButtonID() == 7) { // Reorder
            DBEntry<IQuest> entry = ((PanelButtonStorage<DBEntry<IQuest>>) btn).getStoredValue();
            reorderReq(quest, entry.getID());
            SendChanges();
        }
    }

    private boolean containsReq(IQuest quest, int id) {
        for (int reqID : quest.getRequirements()) if (id == reqID) return true;
        return false;
    }

    private void reorderReq(IQuest quest, int id) {
        int[] orig = quest.getRequirements();

        int indexToShift = -1;
        for (int i = 0; i < orig.length; i++) {
            if (orig[i] == id) {
                indexToShift = i;
                break;
            }
        }
        if (indexToShift < 0) return;

        int indexFrom = (indexToShift - 1 + orig.length) % orig.length;
        int tmp = orig[indexToShift];
        orig[indexToShift] = orig[indexFrom];
        orig[indexFrom] = tmp;

        quest.setRequirements(orig);
    }

    private void removeReq(IQuest quest, int id) {
        int[] orig = quest.getRequirements();
        if (orig.length <= 0) return;
        boolean hasRemoved = false;
        int[] rem = new int[orig.length - 1];
        for (int i = 0; i < orig.length; i++) {
            if (!hasRemoved && orig[i] == id) {
                hasRemoved = true;
                continue;
            } else if (!hasRemoved && i >= rem.length) break;

            rem[!hasRemoved ? i : (i - 1)] = orig[i];
        }

        if (hasRemoved) quest.setRequirements(rem);
    }

    private void addReq(IQuest quest, int id) {
        if (containsReq(quest, id)) return;
        int[] orig = quest.getRequirements();
        int[] added = Arrays.copyOf(orig, orig.length + 1);
        added[orig.length] = id;
        quest.setRequirements(added);
    }

    private void SendChanges() {
        NBTTagCompound payload = new NBTTagCompound();
        NBTTagList dataList = new NBTTagList();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setInteger("questID", questID);
        entry.setTag("config", quest.writeToNBT(new NBTTagCompound()));
        dataList.appendTag(entry);
        payload.setTag("data", dataList);
        payload.setInteger("action", 0);
        NetQuestEdit.sendEdit(payload);
    }
}
