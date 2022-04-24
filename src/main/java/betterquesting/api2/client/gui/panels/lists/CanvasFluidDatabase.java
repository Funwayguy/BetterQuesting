package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelFluidSlot;
import betterquesting.core.BetterQuesting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayDeque;
import java.util.Iterator;

public class CanvasFluidDatabase extends CanvasSearch<FluidStack, Fluid> {
    private final int btnId;

    public CanvasFluidDatabase(IGuiRect rect, int buttonId) {
        super(rect);

        this.btnId = buttonId;
    }

    @Override
    protected Iterator<Fluid> getIterator() {
        return FluidRegistry.getRegisteredFluids().values().iterator();
    }

    @Override
    protected void queryMatches(Fluid fluid, String query, final ArrayDeque<FluidStack> results) {
        if (fluid == null || fluid.getName() == null) {
            return;
        }

        try {
            FluidStack stack = new FluidStack(fluid, 1000);

            if (fluid.getUnlocalizedName().toLowerCase().contains(query) || fluid.getLocalizedName(stack).toLowerCase().contains(query) || fluid.getName().toLowerCase().contains(query)) {
                results.add(stack);
            }
        } catch (Exception e) {
            BetterQuesting.logger.error("An error occured while searching fluid \"" + fluid.getName() + "\" (" + fluid.getClass().getName() + ")", e);
        }
    }

    @Override
    protected boolean addResult(FluidStack stack, int index, int cachedWidth) {
        if (stack == null) {
            return false;
        }

        int x = (index % (cachedWidth / 18)) * 18;
        int y = (index / (cachedWidth / 18)) * 18;

        this.addPanel(new PanelFluidSlot(new GuiRectangle(x, y, 18, 18, 0), btnId, stack));

        return true;
    }
}
