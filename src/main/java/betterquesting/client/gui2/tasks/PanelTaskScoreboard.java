package betterquesting.client.gui2.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.ScoreboardBQ;
import betterquesting.questing.tasks.TaskScoreboard;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.util.vector.Vector4f;

import java.text.DecimalFormat;

public class PanelTaskScoreboard extends CanvasEmpty
{
    private final TaskScoreboard task;
    
    public PanelTaskScoreboard(IGuiRect rect, TaskScoreboard task)
    {
        super(rect);
        this.task = task;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
		int score = ScoreboardBQ.INSTANCE.getScore(QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player), task.scoreName);
		DecimalFormat df = new DecimalFormat("0.##");
		String value = df.format(score/task.conversion) + task.suffix;
		
		if(task.operation.checkValues(score, task.target))
		{
			value = TextFormatting.GREEN + value;
		} else
		{
			value = TextFormatting.RED + value;
		}
		
		String txt2 = TextFormatting.BOLD + value + " " + TextFormatting.RESET + task.operation.GetText() + " " + df.format(task.target/task.conversion) + task.suffix;
		
		// TODO: Add x2 scale when supported
		this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0F, 0.5F, 1F, 0.5F), new GuiPadding(0, -16, 0, 0), 0), task.scoreDisp).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));
		this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0F, 0.5F, 1F, 0.5F), new GuiPadding(0, 0, 0, -16), 0), txt2).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));
    }
}
