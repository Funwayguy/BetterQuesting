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
import betterquesting.questing.rewards.RewardXP;
import net.minecraft.init.Items;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.util.vector.Vector4f;

public class PanelRewardXP extends CanvasMinimum {

    private final IGuiRect initialRect;
    private final RewardXP reward;

    public PanelRewardXP(IGuiRect rect, RewardXP reward) {
        super(rect);
        this.initialRect = rect;
        this.reward = reward;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        int width = initialRect.getWidth();
        this.addPanel(new PanelGeneric(new GuiTransform(new Vector4f(0F, 0F, 0F, 0F), 0, 0, 32, 32, 0), new ItemTexture(new BigItemStack(Items.EXPERIENCE_BOTTLE))));

        String txt2;

        if (reward.amount >= 0) {
            txt2 = TextFormatting.GREEN + "+" + Math.abs(reward.amount);
        } else {
            txt2 = TextFormatting.RED + "-" + Math.abs(reward.amount);
        }

        txt2 += reward.levels ? "L" : "XP";

        this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0F, 0F, 0F, 0F), 36, 2, width - 36, 16, 0), QuestTranslation.translate("bq_standard.gui.experience")).setColor(PresetColor.TEXT_MAIN.getColor()));
        this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0F, 0F, 0F, 0F), 40, 16, width - 40, 16, 0), txt2).setColor(PresetColor.TEXT_MAIN.getColor()));
        recalculateSizes();
    }
}
