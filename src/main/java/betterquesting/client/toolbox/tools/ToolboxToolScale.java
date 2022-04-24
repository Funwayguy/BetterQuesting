package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.api2.client.gui.resources.colors.GuiColorPulse;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.BoxLine;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.network.handlers.NetChapterEdit;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ToolboxToolScale implements IToolboxTool {
    private CanvasQuestLine gui;

    private final NonNullList<GrabEntry> grabList = NonNullList.create();
    private final GuiRectangle scaleBounds = new GuiRectangle(0, 0, 0, 0);
    private IGuiLine selLine = new BoxLine();
    private IGuiColor selCol = new GuiColorPulse(0xFFFFFFFF, 0xFF000000, 2F, 0F);

    @Override
    public void initTool(CanvasQuestLine gui) {
        this.gui = gui;
        grabList.clear();
    }

    @Override
    public void disableTool() {
        if (grabList.size() > 0) {
            for (GrabEntry grab : grabList) {
                IQuestLineEntry qle = gui.getQuestLine().getValue(grab.btn.getStoredValue().getID());

                if (qle != null) {
                    grab.btn.rect.x = qle.getPosX();
                    grab.btn.rect.y = qle.getPosY();
                    grab.btn.rect.w = qle.getSizeX();
                    grab.btn.rect.h = qle.getSizeY();
                }
            }

            grabList.clear();
        }
    }

    @Override
    public void refresh(CanvasQuestLine gui) {
        List<GrabEntry> tmp = new ArrayList<>();

        for (GrabEntry grab : grabList) {
            for (PanelButtonQuest btn : PanelToolController.selected) {
                if (btn.getStoredValue().getID() == grab.btn.getStoredValue().getID()) {
                    tmp.add(new GrabEntry(btn, grab.anchor));
                    break;
                }
            }
        }

        grabList.clear();
        grabList.addAll(tmp);
    }

    @Override
    public void drawCanvas(int mx, int my, float partialTick) {
        if (grabList.size() > 0) {
            int snap = Math.max(1, ToolboxTabMain.INSTANCE.getSnapValue());
            int dx = mx + snap / 2;
            int dy = my + snap / 2;
            dx = ((dx % snap) + snap) % snap;
            dy = ((dy % snap) + snap) % snap;
            dx = (mx + snap / 2) - dx;
            dy = (my + snap / 2) - dy;

            scaleBounds.w = dx - scaleBounds.x;
            scaleBounds.h = dy - scaleBounds.y;

            boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            for (GrabEntry grab : grabList) {
                grab.btn.rect.w = Math.max(1, Math.round(scaleBounds.w * grab.anchor.z));
                grab.btn.rect.h = Math.max(1, Math.round(scaleBounds.h * grab.anchor.w));

                if (shift) // Probably could be implemented better but I'm just going to leave it as now
                {
                    grab.btn.rect.x = grab.sx - grab.btn.rect.w / 2;
                    grab.btn.rect.y = grab.sy - grab.btn.rect.h / 2;
                } else {
                    grab.btn.rect.x = scaleBounds.x + Math.round(scaleBounds.w * grab.anchor.x);
                    grab.btn.rect.y = scaleBounds.y + Math.round(scaleBounds.h * grab.anchor.y);
                }
            }

            if (grabList.size() > 1 && !shift) {
                selLine.drawLine(scaleBounds, scaleBounds, 2, selCol, partialTick);
            }
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
            for (GrabEntry grab : grabList) {
                IQuestLineEntry qle = gui.getQuestLine().getValue(grab.btn.getStoredValue().getID());

                if (qle != null) {
                    grab.btn.rect.x = qle.getPosX();
                    grab.btn.rect.y = qle.getPosY();
                    grab.btn.rect.w = qle.getSizeX();
                    grab.btn.rect.h = qle.getSizeY();
                }
            }

            grabList.clear();
            return true;
        } else if (click != 0 || !gui.getTransform().contains(mx, my)) {
            return false;
        }

        if (grabList.size() > 0) {
            IQuestLine qLine = gui.getQuestLine();
            int lID = QuestLineDatabase.INSTANCE.getID(qLine);
            for (GrabEntry grab : grabList) {
                IQuestLineEntry qle = gui.getQuestLine().getValue(grab.btn.getStoredValue().getID());
                if (qle != null) {
                    qle.setPosition(grab.btn.rect.x, grab.btn.rect.y);
                    qle.setSize(grab.btn.rect.w, grab.btn.rect.h);
                }
            }

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

            grabList.clear();
            return true;
        }

        PanelButtonQuest btnClicked = gui.getButtonAt(mx, my);

        if (btnClicked != null) // Pickup the group or the single one if none are selected
        {
            if (PanelToolController.selected.size() > 0) {
                if (!PanelToolController.selected.contains(btnClicked)) return false;

                boolean first = true;
                for (PanelButtonQuest btn : PanelToolController.selected) {
                    if (first) {
                        scaleBounds.x = btn.rect.x;
                        scaleBounds.y = btn.rect.y;
                        scaleBounds.w = btn.rect.w;
                        scaleBounds.h = btn.rect.h;
                        first = false;
                    } else {
                        scaleBounds.x = Math.min(scaleBounds.x, btn.rect.x);
                        scaleBounds.y = Math.min(scaleBounds.y, btn.rect.y);
                        scaleBounds.w = Math.max(scaleBounds.x + scaleBounds.w, btn.rect.x + btn.rect.w) - scaleBounds.x;
                        scaleBounds.h = Math.max(scaleBounds.y + scaleBounds.h, btn.rect.y + btn.rect.h) - scaleBounds.y;
                    }
                }

                for (PanelButtonQuest btn : PanelToolController.selected) {
                    float x = (btn.rect.x - scaleBounds.x) / (float) scaleBounds.w;
                    float y = (btn.rect.y - scaleBounds.y) / (float) scaleBounds.h;
                    float w = btn.rect.w / (float) scaleBounds.w;
                    float h = btn.rect.h / (float) scaleBounds.h;
                    grabList.add(new GrabEntry(btn, new Vector4f(x, y, w, h)));
                }
            } else {
                scaleBounds.x = btnClicked.rect.x;
                scaleBounds.y = btnClicked.rect.y;
                scaleBounds.w = btnClicked.rect.w;
                scaleBounds.h = btnClicked.rect.h;
                grabList.add(new GrabEntry(btnClicked, new Vector4f(0F, 0F, 1F, 1F)));
            }

            return true;
        }

        return false;
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
    public boolean onKeyPressed(char c, int key) {
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
        private final Vector4f anchor;
        private final int sx;
        private final int sy;

        private GrabEntry(PanelButtonQuest btn, Vector4f anchor) {
            this.btn = btn;
            this.anchor = anchor;
            this.sx = btn.rect.x + btn.rect.w / 2;
            this.sy = btn.rect.y + btn.rect.h / 2;
        }
    }
}
