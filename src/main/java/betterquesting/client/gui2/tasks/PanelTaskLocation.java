package betterquesting.client.gui2.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasMinimum;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.questing.tasks.TaskLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;

public class PanelTaskLocation extends CanvasMinimum {

    private final IGuiRect initialRect;
    private final TaskLocation task;

    public PanelTaskLocation(IGuiRect rect, TaskLocation task) {
        super(rect);
        this.initialRect = rect;
        this.task = task;
    }

    @Override
    public void initPanel() {
        super.initPanel();
        int width = initialRect.getWidth();

        String desc = QuestTranslation.translate(task.name);

        if (!task.hideInfo) {
            desc += " (" + getDimName(task.dim) + ")";

            if (task.range >= 0) {
                desc += "\n" + QuestTranslation.translate("bq_standard.gui.location", "(" + task.x + ", " + task.y + ", " + task.z + ")");
                desc += "\n" + QuestTranslation.translate("bq_standard.gui.distance", (int) Minecraft.getMinecraft().player.getDistance(task.x, task.y, task.z) + "m");
            }

            if (!StringUtils.isEmpty(task.biome)) {
                Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(task.biome));
                desc += "\n" + QuestTranslation.translate("bq_standard.gui.biome", (biome == null ? "?" : biome.getBiomeName()));
            }

            if (!StringUtils.isNotEmpty(task.structure)) {
                desc += "\n" + QuestTranslation.translate("bq_standard.gui.structure", task.structure);
            }
        }

        if (task.isComplete(QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player))) {
            desc += "\n" + TextFormatting.BOLD + TextFormatting.GREEN + QuestTranslation.translate("bq_standard.gui.found");
        } else {
            desc += "\n" + TextFormatting.BOLD + TextFormatting.RED + QuestTranslation.translate("bq_standard.gui.undiscovered");
        }

        int textHeight = (StringUtils.countMatches(desc, "\n") + 1) * 12;
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_LEFT, 0, 0, width, textHeight, 0), desc).setColor(PresetColor.TEXT_MAIN.getColor()));

        IGuiTexture texCompass = new IGuiTexture() {
            @Override
            public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick) {
                drawTexture(x, y, width, height, zDepth, partialTick, null);
            }

            @Override
            public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color) {
                Minecraft mc = Minecraft.getMinecraft();

                double la = Math.atan2(task.z - mc.player.posZ, task.x - mc.player.posX);
                int radius = width / 2 - 12;
                int cx = x + width / 2;
                int cy = y + height / 2;
                int dx = (int) (Math.cos(la) * radius);
                int dy = (int) (Math.sin(la) * -radius);
                int txtClr = color == null ? 0xFFFFFFFF : color.getRGB();

                Gui.drawRect(cx - radius, cy - radius, cx + radius, cy + radius, Color.BLACK.getRGB());
                RenderUtils.DrawLine(cx - radius, cy - radius, cx + radius, cy - radius, 4, txtClr);
                RenderUtils.DrawLine(cx - radius, cy - radius, cx - radius, cy + radius, 4, txtClr);
                RenderUtils.DrawLine(cx + radius, cy + radius, cx + radius, cy - radius, 4, txtClr);
                RenderUtils.DrawLine(cx + radius, cy + radius, cx - radius, cy + radius, 4, txtClr);
                mc.fontRenderer.drawString(TextFormatting.BOLD + "N", cx - 4, cy - radius - 9, txtClr);
                mc.fontRenderer.drawString(TextFormatting.BOLD + "S", cx - 4, cy + radius + 2, txtClr);
                mc.fontRenderer.drawString(TextFormatting.BOLD + "E", cx + radius + 2, cy - 4, txtClr);
                mc.fontRenderer.drawString(TextFormatting.BOLD + "W", cx - radius - 8, cy - 4, txtClr);

                if (task.hideInfo || task.range < 0 || mc.player.dimension != task.dim) {
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(2F, 2F, 2F);
                    mc.fontRenderer.drawString(TextFormatting.BOLD + "?", cx / 2 - 4, cy / 2 - 4, Color.RED.getRGB());
                    GlStateManager.popMatrix();
                } else {
                    RenderUtils.DrawLine(cx, cy, cx + dx, cy - dy, 4, Color.RED.getRGB());
                }
            }

            @Override
            public ResourceLocation getTexture() {
                return null;
            }

            @Override
            public IGuiRect getBounds() {
                return null;
            }
        };

        int innerSize = Math.min(Math.min(initialRect.getWidth(), 128), initialRect.getHeight() - textHeight);
        PanelGeneric panelCompass = new PanelGeneric(new GuiTransform(GuiAlign.TOP_LEFT, (width - innerSize) / 2, textHeight, innerSize, innerSize, 0), texCompass, PresetColor.TEXT_MAIN.getColor());

        this.addPanel(panelCompass);
        panelCompass.setEnabled(task.range >= 0 && !task.hideInfo);
        recalculateSizes();
    }

    private static String getDimName(int dim) {
        try {
            return DimensionType.getById(dim).getName();
        } catch (Exception e) {
            return "?";
        }
    }
}
