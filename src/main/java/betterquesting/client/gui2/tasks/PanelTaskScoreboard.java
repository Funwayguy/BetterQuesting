package betterquesting.client.gui2.tasks;

import betterquesting.ScoreboardBQ;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasMinimum;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.questing.tasks.TaskScoreboard;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.text.DecimalFormat;

public class PanelTaskScoreboard extends CanvasMinimum {

    private final IGuiRect initialRect;
    private final TaskScoreboard task;

    public PanelTaskScoreboard(IGuiRect rect, TaskScoreboard task) {
        super(rect);
        this.initialRect = rect;
        this.task = task;
    }

    @Override
    public void initPanel() {
        super.initPanel();
        int width = initialRect.getWidth();

        int score = ScoreboardBQ.INSTANCE.getScore(QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player), task.scoreName);
        DecimalFormat df = new DecimalFormat("0.##");
        String value = df.format(score / task.conversion) + task.suffix;

        if (task.operation.checkValues(score, task.target)) {
            value = TextFormatting.GREEN + value;
        } else {
            value = TextFormatting.RED + value;
        }

        String txt2 = TextFormatting.BOLD + value + " " + TextFormatting.RESET + task.operation.GetText() + " " + df.format(task.target / task.conversion) + task.suffix;

        // TODO: Add x2 scale when supported
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_LEFT, 0, 0, width, 16, 0), task.scoreDisp).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_LEFT, 0, 16, width, 16, 0), txt2).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));
        recalculateSizes();
    }
}
