package betterquesting.client.gui2.rewards;

import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasMinimum;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.questing.rewards.RewardScoreboard;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.util.vector.Vector4f;

public class PanelRewardScoreboard extends CanvasMinimum {

    private final IGuiRect initialRect;
    private final RewardScoreboard reward;

    public PanelRewardScoreboard(IGuiRect rect, RewardScoreboard reward) {
        super(rect);
        this.initialRect = rect;
        this.reward = reward;
    }

    @Override
    public void initPanel() {
        super.initPanel();
        int width = initialRect.getWidth();
        this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0F, 0F, 0F, 0F), 0, 0, width, 12, 0), reward.score).setColor(PresetColor.TEXT_MAIN.getColor()));

        String txt2 = TextFormatting.BOLD.toString();
        if (!reward.relative) {
            txt2 += "= " + reward.value;
        } else if (reward.value >= 0) {
            txt2 += TextFormatting.GREEN + "+ " + Math.abs(reward.value);
        } else {
            txt2 += TextFormatting.RED + "- " + Math.abs(reward.value);
        }

        this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0F, 0F, 0F, 0F), 4, 12, width - 4, 12, 0), txt2).setColor(PresetColor.TEXT_MAIN.getColor()));
        recalculateSizes();
    }
}
