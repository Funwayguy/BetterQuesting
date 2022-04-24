package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.network.handlers.NetChapterEdit;
import betterquesting.network.handlers.NetQuestEdit;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

import java.util.*;

public class ToolboxToolCopy implements IToolboxTool {
    private CanvasQuestLine gui = null;

    private final NonNullList<GrabEntry> grabList = NonNullList.create();

    @Override
    public void initTool(CanvasQuestLine gui) {
        this.gui = gui;
        grabList.clear();
    }

    @Override
    public void disableTool() {
        grabList.clear();
    }

    @Override
    public void refresh(CanvasQuestLine gui) {
        if (grabList.size() <= 0) return;

        List<GrabEntry> tmp = new ArrayList<>();

        for (GrabEntry grab : grabList) {
            for (PanelButtonQuest btn : PanelToolController.selected) {
                if (btn.getStoredValue().getID() == grab.btn.getStoredValue().getID()) {
                    tmp.add(new GrabEntry(btn, grab.offX, grab.offY));
                    break;
                }
            }
        }

        grabList.clear();
        grabList.addAll(tmp);
    }

    @Override
    public void drawCanvas(int mx, int my, float partialTick) {
        if (grabList.size() <= 0) return;

        int snap = Math.max(1, ToolboxTabMain.INSTANCE.getSnapValue());
        int dx = mx;
        int dy = my;
        dx = ((dx % snap) + snap) % snap;
        dy = ((dy % snap) + snap) % snap;
        dx = mx - dx;
        dy = my - dy;

        for (GrabEntry grab : grabList) {
            grab.btn.rect.x = dx + grab.offX;
            grab.btn.rect.y = dy + grab.offY;
            grab.btn.drawPanel(dx, dy, partialTick);
        }
    }

    @Override
    public void drawOverlay(int mx, int my, float partialTick) {
        if (grabList.size() > 0) ToolboxTabMain.INSTANCE.drawGrid(gui);
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        return grabList.size() <= 0 ? null : Collections.emptyList();
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        if (click == 1 && grabList.size() > 0) {
            grabList.clear();
            return true;
        } else if (click != 0 || !gui.getTransform().contains(mx, my)) {
            return false;
        }

        if (grabList.size() <= 0) {
            PanelButtonQuest btnClicked = gui.getButtonAt(mx, my);

            if (btnClicked != null) // Pickup the group or the single one if none are selected
            {
                if (PanelToolController.selected.size() > 0) {
                    if (!PanelToolController.selected.contains(btnClicked)) return false;

                    for (PanelButtonQuest btn : PanelToolController.selected) {
                        GuiRectangle rect = new GuiRectangle(btn.rect);
                        grabList.add(new GrabEntry(new PanelButtonQuest(rect, -1, "", btn.getStoredValue()), rect.x - btnClicked.rect.x, rect.y - btnClicked.rect.y));
                    }
                } else {
                    grabList.add(new GrabEntry(new PanelButtonQuest(new GuiRectangle(btnClicked.rect), -1, "", btnClicked.getStoredValue()), 0, 0));
                }

                return true;
            }

            return false;
        }

        // Pre-sync
        IQuestLine qLine = gui.getQuestLine();
        int lID = QuestLineDatabase.INSTANCE.getID(qLine);

        int[] nextIDs = getNextIDs(grabList.size());
        HashMap<Integer, Integer> remappedIDs = new HashMap<>();

        for (int i = 0; i < grabList.size(); i++)
            remappedIDs.put(grabList.get(i).btn.getStoredValue().getID(), nextIDs[i]);

        NBTTagList qdList = new NBTTagList();

        for (int i = 0; i < grabList.size(); i++) {
            GrabEntry grab = grabList.get(i);
            IQuest quest = grab.btn.getStoredValue().getValue();
            int qID = nextIDs[i];

            if (qLine.getValue(qID) == null)
                qLine.add(qID, new QuestLineEntry(grab.btn.rect.x, grab.btn.rect.y, grab.btn.rect.w, grab.btn.rect.h));

            NBTTagCompound questTags = quest.writeToNBT(new NBTTagCompound());

            int[] oldIDs = Arrays.copyOf(quest.getRequirements(), quest.getRequirements().length);

            for (int n = 0; n < oldIDs.length; n++) {
                if (remappedIDs.containsKey(oldIDs[n])) {
                    oldIDs[n] = remappedIDs.get(oldIDs[n]);
                }
            }

            // We can't tamper with the original so we change it in NBT post-write
            questTags.setIntArray("preRequisites", oldIDs);

            NBTTagCompound tagEntry = new NBTTagCompound();
            tagEntry.setInteger("questID", qID);
            tagEntry.setTag("config", questTags);
            qdList.appendTag(tagEntry);
        }

        grabList.clear();

        // Send new quests
        NBTTagCompound quPayload = new NBTTagCompound();
        quPayload.setTag("data", qdList);
        quPayload.setInteger("action", 3);
        NetQuestEdit.sendEdit(quPayload);

        // Send quest line edits
        NBTTagCompound chPayload = new NBTTagCompound();
        NBTTagList cdList = new NBTTagList();
        NBTTagCompound tagEntry = new NBTTagCompound();
        tagEntry.setInteger("chapterID", lID);
        tagEntry.setTag("config", qLine.writeToNBT(new NBTTagCompound(), null));
        cdList.appendTag(tagEntry);
        chPayload.setTag("data", cdList);
        chPayload.setInteger("action", 0);
        NetChapterEdit.sendEdit(chPayload);

        return true;
    }

    private int[] getNextIDs(int num) {
        List<DBEntry<IQuest>> listDB = QuestDatabase.INSTANCE.getEntries();
        int[] nxtIDs = new int[num];

        if (listDB.size() <= 0 || listDB.get(listDB.size() - 1).getID() == listDB.size() - 1) {
            for (int i = 0; i < num; i++) nxtIDs[i] = listDB.size() + i;
            return nxtIDs;
        }

        int n1 = 0;
        int n2 = 0;
        for (int i = 0; i < num; i++) {
            while (n2 < listDB.size() && listDB.get(n2).getID() == n1) {
                n1++;
                n2++;
            }

            nxtIDs[i] = n1++;
        }

        return nxtIDs;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        return false;
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        return false;
    }

    @Override
    public boolean onKeyPressed(char c, int keyCode) {
        return grabList.size() > 0;
    }

    @Override
    public boolean clampScrolling() {
        return grabList.size() <= 0;
    }

    @Override
    public void onSelection(NonNullList<PanelButtonQuest> buttons) {
    }

    @Override
    public boolean useSelection() {
        return grabList.size() <= 0;
    }

    private class GrabEntry {
        private final PanelButtonQuest btn;
        private final int offX;
        private final int offY;

        private GrabEntry(PanelButtonQuest btn, int offX, int offY) {
            this.btn = btn;
            this.offX = offX;
            this.offY = offY;
        }
    }
}
