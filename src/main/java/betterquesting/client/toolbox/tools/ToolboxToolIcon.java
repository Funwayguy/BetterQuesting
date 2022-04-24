package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.client.gui2.editors.nbt.GuiItemSelection;
import betterquesting.network.handlers.NetQuestEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.List;

public class ToolboxToolIcon implements IToolboxTool {
    private CanvasQuestLine gui;

    @Override
    public void initTool(CanvasQuestLine gui) {
        this.gui = gui;
    }

    @Override
    public void disableTool() {
    }

    @Override
    public void refresh(CanvasQuestLine gui) {
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        if (click != 0 || !gui.getTransform().contains(mx, my)) return false;

        PanelButtonQuest btn = gui.getButtonAt(mx, my);

        if (btn == null) return false;
        if (PanelToolController.selected.size() > 0 && !PanelToolController.selected.contains(btn)) return false;

        List<PanelButtonQuest> btnList = PanelToolController.selected.size() > 0 ? PanelToolController.selected : Collections.singletonList(btn);
        changeIcon(btnList, btn.getStoredValue().getValue().getProperty(NativeProps.ICON));
        return true;
    }

    private void changeIcon(List<PanelButtonQuest> list, BigItemStack refItem) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayGuiScreen(new GuiItemSelection(mc.currentScreen, refItem, value -> {
            NBTTagList dataList = new NBTTagList();
            for (PanelButtonQuest b : list) {
                b.getStoredValue().getValue().setProperty(NativeProps.ICON, value);

                NBTTagCompound entry = new NBTTagCompound();
                entry.setInteger("questID", b.getStoredValue().getID());
                entry.setTag("config", b.getStoredValue().getValue().writeToNBT(new NBTTagCompound()));
                dataList.appendTag(entry);
            }

            NBTTagCompound payload = new NBTTagCompound();
            payload.setTag("data", dataList);
            payload.setInteger("action", 0);
            NetQuestEdit.sendEdit(payload);
        }));
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
        if (PanelToolController.selected.size() <= 0 || key != Keyboard.KEY_RETURN) return false;
        changeIcon(PanelToolController.selected, PanelToolController.selected.get(0).getStoredValue().getValue().getProperty(NativeProps.ICON));
        return true;
    }

    @Override
    public void drawCanvas(int mx, int my, float partialTick) {
    }

    @Override
    public void drawOverlay(int mx, int my, float partialTick) {
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        return null;
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
        return true;
    }
}
