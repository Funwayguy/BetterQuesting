package betterquesting.client.gui2.rewards;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasMinimum;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.questing.rewards.RewardItem;

public class PanelRewardItem extends CanvasMinimum {

    private final IGuiRect initialRect;
    private final RewardItem reward;

    public PanelRewardItem(IGuiRect rect, RewardItem reward) {
        super(rect);
        this.initialRect = rect;
        this.reward = reward;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        int listWidth = initialRect.getWidth();
        for (int i = 0; i < reward.items.size(); i++) {
            BigItemStack stack = reward.items.get(i);
            PanelItemSlot is = new PanelItemSlot(new GuiRectangle(0, i * 18, 18, 18, 0), -1, stack, true);
            this.addPanel(is);

            this.addPanel(new PanelTextBox(new GuiRectangle(22, i * 18 + 4, listWidth - 22, 14, 0), stack.stackSize + " " + stack.getBaseStack().getDisplayName()).setColor(PresetColor.TEXT_MAIN.getColor()));
        }
        recalculateSizes();
    }
}
