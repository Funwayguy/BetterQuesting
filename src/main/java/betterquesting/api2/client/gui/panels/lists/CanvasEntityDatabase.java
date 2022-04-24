package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CanvasEntityDatabase extends CanvasSearch<EntityEntry, EntityEntry> {
    private final int btnId;

    public CanvasEntityDatabase(IGuiRect rect, int buttonId) {
        super(rect);
        this.btnId = buttonId;
    }

    @Override
    protected Iterator<EntityEntry> getIterator() {
        List<EntityEntry> list = new ArrayList<>(ForgeRegistries.ENTITIES.getValuesCollection());
        list.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        return list.iterator();
    }

    @Override
    protected void queryMatches(EntityEntry ee, String query, final ArrayDeque<EntityEntry> results) {
        if (ee == null || ee.getRegistryName() == null) return;

        String qlc = query.toLowerCase();

        if (ee.getRegistryName().toString().toLowerCase().contains(qlc) || ee.getName().toLowerCase().contains(qlc)) {
            results.add(ee);
        }
    }

    @Override
    protected boolean addResult(EntityEntry ee, int index, int cachedWidth) {
        if (ee == null) {
            return false;
        }

        this.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, index * 16, cachedWidth, 16, 0), btnId, ee.getName(), ee));

        return true;
    }
}
