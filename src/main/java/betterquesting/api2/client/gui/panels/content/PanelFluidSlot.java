package betterquesting.api2.client.gui.panels.content;

import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.ColorTexture;
import betterquesting.api2.client.gui.resources.textures.FluidTexture;
import betterquesting.api2.client.gui.resources.textures.LayeredTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class PanelFluidSlot extends PanelButtonStorage<FluidStack> {
    private final boolean showCount;

    public PanelFluidSlot(IGuiRect rect, int id, FluidStack value) {
        this(rect, id, value, false);
    }

    public PanelFluidSlot(IGuiRect rect, int id, FluidStack value, boolean showCount) {
        super(rect, id, "", value);
        this.showCount = showCount;

        this.setTextures(PresetTexture.ITEM_FRAME.getTexture(), PresetTexture.ITEM_FRAME.getTexture(), new LayeredTexture(PresetTexture.ITEM_FRAME.getTexture(), new ColorTexture(PresetColor.ITEM_HIGHLIGHT.getColor(), new GuiPadding(1, 1, 1, 1))));
        this.setStoredValue(value); // Need to run this again because of the instatiation order of showCount

    }

    @Override
    public PanelFluidSlot setStoredValue(FluidStack value) {
        super.setStoredValue(value);

        if (value != null) {
            this.setIcon(new FluidTexture(value, showCount, true), 1);
            List<String> tooltip = new ArrayList<>();
            tooltip.add(value.getLocalizedName());
            tooltip.add(TextFormatting.GRAY.toString() + value.amount + "mB");
            this.setTooltip(tooltip);
        } else {
            this.setIcon(null);
            this.setTooltip(null);
        }

        return this;
    }
}
