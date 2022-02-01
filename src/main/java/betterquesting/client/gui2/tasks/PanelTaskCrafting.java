package betterquesting.client.gui2.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasMinimum;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.GuiTextureColored;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskCrafting;
import mezz.jei.Internal;
import mezz.jei.api.recipe.IFocus.Mode;
import mezz.jei.gui.Focus;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Optional.Method;

import java.util.UUID;

public class PanelTaskCrafting extends CanvasMinimum {

    private final IGuiRect initialRect;
    private final TaskCrafting task;

    public PanelTaskCrafting(IGuiRect rect, TaskCrafting task) {
        super(rect);
        this.initialRect = rect;
        this.task = task;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        UUID uuid = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
        int[] progress = task.getUsersProgress(uuid);
        boolean isComplete = task.isComplete(uuid);

        IGuiTexture txTick = new GuiTextureColored(PresetIcon.ICON_TICK.getTexture(), new GuiColorStatic(0xFF00FF00));
        IGuiTexture txCross = new GuiTextureColored(PresetIcon.ICON_CROSS.getTexture(), new GuiColorStatic(0xFFFF0000));

        this.addPanel(new PanelGeneric(new GuiRectangle(0, 0, 16, 16, 0), new ItemTexture(new BigItemStack(Blocks.CRAFTING_TABLE))));
        this.addPanel(new PanelGeneric(new GuiRectangle(10, 10, 6, 6, 0), task.allowCraft ? txTick : txCross));

        this.addPanel(new PanelGeneric(new GuiRectangle(24, 0, 16, 16, 0), new ItemTexture(new BigItemStack(Blocks.FURNACE))));
        this.addPanel(new PanelGeneric(new GuiRectangle(34, 10, 6, 6, 0), task.allowSmelt ? txTick : txCross));

        this.addPanel(new PanelGeneric(new GuiRectangle(48, 0, 16, 16, 0), new ItemTexture(new BigItemStack(Blocks.ANVIL))));
        this.addPanel(new PanelGeneric(new GuiRectangle(58, 10, 6, 6, 0), task.allowAnvil ? txTick : txCross));

        int listW = initialRect.getWidth();

        for (int i = 0; i < task.requiredItems.size(); i++) {
            BigItemStack stack = task.requiredItems.get(i);

            PanelItemSlot slot = new PanelItemSlot(new GuiRectangle(0, i * 28 + 24, 28, 28, 0), -1, stack, false, true);
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

            PanelTextBox text = new PanelTextBox(new GuiRectangle(36, i * 28 + 24, listW - 36, 28, 0), sb.toString());
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
