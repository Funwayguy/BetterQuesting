package betterquesting.client.gui2.rewards;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasMinimum;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.questing.rewards.RewardCommand;
import net.minecraft.init.Blocks;
import org.lwjgl.util.vector.Vector4f;

public class PanelRewardCommand extends CanvasMinimum {

    private final RewardCommand reward;
    private final IGuiRect initialRect;

    public PanelRewardCommand(IGuiRect rect, RewardCommand reward) {
        super(rect);
        initialRect = rect;
        this.reward = reward;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        int width = initialRect.getWidth();
        if (!reward.hideIcon)
            this.addPanel(new PanelGeneric(new GuiTransform(new Vector4f(0F, 0F, 0F, 0F), 0, 0, 32, 32, 0), new ItemTexture(new BigItemStack(Blocks.COMMAND_BLOCK))));
        String txt = QuestTranslation.translate(reward.desc);
        this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0F, 0F, 0F, 0F), 40, 0, width - 40, 32, 0), txt).setColor(PresetColor.TEXT_MAIN.getColor()));
        recalculateSizes();
    }
}
