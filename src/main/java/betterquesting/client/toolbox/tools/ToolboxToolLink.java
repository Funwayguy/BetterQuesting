package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.network.handlers.NetQuestEdit;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToolboxToolLink implements IToolboxTool {
    private CanvasQuestLine gui;
    private final NonNullList<PanelButtonQuest> linking = NonNullList.create();
    private final GuiRectangle mouseRect = new GuiRectangle(0, 0, 0, 0);

    @Override
    public void initTool(CanvasQuestLine gui) {
        this.gui = gui;
        linking.clear();
    }

    @Override
    public void disableTool() {
        linking.clear();
    }

    @Override
    public void refresh(CanvasQuestLine gui) {
        if (linking.size() <= 0) return;

        List<PanelButtonQuest> tmp = new ArrayList<>();

        for (PanelButtonQuest b1 : linking) {
            for (PanelButtonQuest b2 : gui.getQuestButtons())
                if (b1.getStoredValue().getID() == b2.getStoredValue().getID()) tmp.add(b2);
        }

        linking.clear();
        linking.addAll(tmp);
    }

    @Override
    public void drawCanvas(int mx, int my, float partialTick) {
        if (linking.size() <= 0) return;

        mouseRect.x = mx;
        mouseRect.y = my;

        for (PanelButtonQuest btn : linking) {
            PresetLine.QUEST_COMPLETE.getLine().drawLine(btn.rect, mouseRect, 2, PresetColor.QUEST_LINE_COMPLETE.getColor(), partialTick);
        }
    }

    @Override
    public void drawOverlay(int mx, int my, float partialTick) {
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        return null;
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        if (click == 1 && linking.size() > 0) {
            linking.clear();
            return true;
        } else if (click != 0 || !gui.getTransform().contains(mx, my)) {
            return false;
        }

        if (linking.size() <= 0) {
            PanelButtonQuest btn = gui.getButtonAt(mx, my);
            if (btn == null) return false;

            if (PanelToolController.selected.size() > 0) {
                if (!PanelToolController.selected.contains(btn)) return false;
                linking.addAll(PanelToolController.selected);
                return true;
            }

            linking.add(btn);
            return true;
        } else {
            PanelButtonQuest b2 = gui.getButtonAt(mx, my);

            if (b2 == null) return false;
            linking.remove(b2);

            if (linking.size() > 0) {
                IQuest q2 = b2.getStoredValue().getValue();
                boolean mod2 = false;

                NBTTagList dataList = new NBTTagList();

                for (PanelButtonQuest b1 : linking) {
                    IQuest q1 = b1.getStoredValue().getValue();
                    boolean mod1 = false;

                    // Don't have to worry about the lines anymore. The panel is getting refereshed anyway
                    if (!containsReq(q2, b1.getStoredValue().getID()) && !containsReq(q1, b2.getStoredValue().getID())) {
                        mod2 = addReq(q2, b1.getStoredValue().getID()) || mod2;
                    } else {
                        mod2 = removeReq(q2, b1.getStoredValue().getID()) || mod2;
                        mod1 = removeReq(q1, b2.getStoredValue().getID());
                    }

                    if (mod1) {
                        NBTTagCompound entry = new NBTTagCompound();
                        entry.setInteger("questID", b1.getStoredValue().getID());
                        entry.setTag("config", b1.getStoredValue().getValue().writeToNBT(new NBTTagCompound()));
                        dataList.appendTag(entry);
                    }
                }

                if (mod2) {
                    NBTTagCompound entry = new NBTTagCompound();
                    entry.setInteger("questID", b2.getStoredValue().getID());
                    entry.setTag("config", q2.writeToNBT(new NBTTagCompound()));
                    dataList.appendTag(entry);
                }

                NBTTagCompound payload = new NBTTagCompound();
                payload.setTag("data", dataList);
                payload.setInteger("action", 0);
                NetQuestEdit.sendEdit(payload);

                linking.clear();
                return true;
            }

            return false;
        }
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
        return false;
    }

    @Override
    public boolean clampScrolling() {
        return true;
    }

    @Override
    public void onSelection(NonNullList<PanelButtonQuest> buttons) {
    }

    @Override
    public boolean useSelection() {
        return linking.size() <= 0;
    }

    private boolean containsReq(IQuest quest, int id) {
        for (int reqID : quest.getRequirements()) if (id == reqID) return true;
        return false;
    }

    private boolean removeReq(IQuest quest, int id) {
        int[] orig = quest.getRequirements();
        if (orig.length <= 0) return false;
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
        return hasRemoved;
    }

    private boolean addReq(IQuest quest, int id) {
        if (containsReq(quest, id)) return false;
        int[] orig = quest.getRequirements();
        int[] added = Arrays.copyOf(orig, orig.length + 1);
        added[orig.length] = id;
        quest.setRequirements(added);
        return true;
    }
}
