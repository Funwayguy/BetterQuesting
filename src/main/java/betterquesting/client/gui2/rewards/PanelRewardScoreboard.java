package betterquesting.client.gui2.rewards;

import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.questing.rewards.RewardScoreboard;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.util.vector.Vector4f;

public class PanelRewardScoreboard extends CanvasEmpty
{
    private final RewardScoreboard reward;
    
    public PanelRewardScoreboard(IGuiRect rect, RewardScoreboard reward)
    {
        super(rect);
        this.reward = reward;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0F, 0.5F, 1F, 0.5F), new GuiPadding(0, -16, 0, 0), 0), reward.score).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));
		String txt2 = TextFormatting.BOLD.toString();
		
		if(!reward.relative)
		{
			txt2 += "= " + reward.value;
		} else if(reward.value >= 0)
		{
			txt2 += TextFormatting.GREEN + "+ " + Math.abs(reward.value);
		} else
		{
			txt2 += TextFormatting.RED + "- " + Math.abs(reward.value);
		}
		
        this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0F, 0.5F, 1F, 0.5F), new GuiPadding(0, 0, 0, -16), 0), txt2).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));
    }
}
