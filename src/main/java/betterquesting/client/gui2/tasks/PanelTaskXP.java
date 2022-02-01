package betterquesting.client.gui2.tasks;

import betterquesting.XPHelper;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.controls.io.ValueFuncIO;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasMinimum;
import betterquesting.api2.client.gui.panels.bars.PanelHBarFill;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.questing.tasks.TaskXP;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.util.text.TextFormatting;

public class PanelTaskXP extends CanvasMinimum {

    private final IGuiRect initialRect;
    private final TaskXP task;

    public PanelTaskXP(IGuiRect rect, TaskXP task) {
        super(rect);
        this.initialRect = rect;
        this.task = task;
    }

    @Override
    public void initPanel() {
        super.initPanel();
        int width = initialRect.getWidth();

        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, (width - 32) / 2, 0, 32, 32, 0), new ItemTexture(new BigItemStack(Items.EXPERIENCE_BOTTLE))));

        long xp = task.getUsersProgress(QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player));
        xp = !task.levels ? xp : XPHelper.getXPLevel(xp);
        final float xpPercent = (float) ((double) xp / (double) task.amount);

        int barWidth = Math.min(128, width);
        PanelHBarFill fillBar = new PanelHBarFill(new GuiTransform(GuiAlign.TOP_LEFT, (width - barWidth) / 2, 32, barWidth, 16, 0));
        fillBar.setFillColor(new GuiColorStatic(0xFF00FF00));
        fillBar.setFillDriver(new ValueFuncIO<>(() -> xpPercent));
        this.addPanel(fillBar);

        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_LEFT, 0, 36, width, 16, -1), TextFormatting.BOLD + "" + xp + "/" + task.amount + (task.levels ? "L" : "XP")).setAlignment(1));
        recalculateSizes();
    }
}
