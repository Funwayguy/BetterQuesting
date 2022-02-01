package betterquesting.client.gui2.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasMinimum;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskRetrieval;
import mezz.jei.Internal;
import mezz.jei.api.recipe.IFocus.Mode;
import mezz.jei.gui.Focus;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Optional.Method;

import java.util.UUID;

public class PanelTaskRetrieval extends CanvasMinimum {

    private final IGuiRect initialRect;
    private final TaskRetrieval task;

    public PanelTaskRetrieval(IGuiRect rect, TaskRetrieval task) {
        super(rect);
        this.initialRect = rect;
        this.task = task;
    }

    @Override
    public void initPanel() {
        super.initPanel();
        int listW = initialRect.getWidth();

        UUID uuid = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
        int[] progress = task.getUsersProgress(uuid);
        boolean isComplete = task.isComplete(uuid);

        String sCon = (task.consume ? TextFormatting.RED : TextFormatting.GREEN) + QuestTranslation.translate(task.consume ? "gui.yes" : "gui.no");

        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, 0, 0, listW, 16, 0), QuestTranslation.translate("bq_standard.btn.consume", sCon)).setColor(PresetColor.TEXT_MAIN.getColor()));

        for (int i = 0; i < task.requiredItems.size(); i++) {
            BigItemStack stack = task.requiredItems.get(i);

            PanelItemSlot slot = new PanelItemSlot(new GuiRectangle(0, i * 32 + 16, 28, 28, 0), -1, stack, false, true);
            if (BetterQuesting.hasJEI) slot.setCallback(value -> lookupRecipe(value.getBaseStack()));
            this.addPanel(slot);

            StringBuilder sb = new StringBuilder();

            sb.append(stack.getBaseStack().getDisplayName());

            if (stack.hasOreDict()) sb.append(" (").append(stack.getOreDict()).append(")");

            sb.append("\n").append(progress[i]).append("/").append(stack.stackSize).append("\n");

            if (isComplete || progress[i] >= stack.stackSize) {
                sb.append(TextFormatting.GREEN).append(QuestTranslation.translate("betterquesting.tooltip.complete"));
            } else {
                sb.append(TextFormatting.RED).append(QuestTranslation.translate("betterquesting.tooltip.incomplete"));
            }

            PanelTextBox text = new PanelTextBox(new GuiRectangle(32, i * 32 + 16, listW - 28, 28, 0), sb.toString());
            text.setColor(PresetColor.TEXT_MAIN.getColor());
            this.addPanel(text);
        }
        recalculateSizes();
    }

    @Method(modid = "jei")
    private void lookupRecipe(ItemStack stack) {
        if (stack == null || stack.isEmpty() || Internal.getRuntime() == null) return;
        Internal.getRuntime().getRecipesGui().show(new Focus<>(Mode.OUTPUT, stack));
    }
}
