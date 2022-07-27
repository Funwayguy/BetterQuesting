package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import java.util.*;
import net.minecraft.entity.EntityList;
import net.minecraft.util.StringUtils;

public class CanvasEntityDatabase extends CanvasSearch<String, String> {
    private final int btnId;

    public CanvasEntityDatabase(IGuiRect rect, int buttonId) {
        super(rect);
        this.btnId = buttonId;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Iterator<String> getIterator() {
        List<String> list = new ArrayList<>((Set<String>) EntityList.stringToClassMapping.keySet());
        Collections.sort(list);
        return list.iterator();
    }

    @Override
    protected void queryMatches(String ee, String query, final ArrayDeque<String> results) {
        if (StringUtils.isNullOrEmpty(ee)) return;

        if (ee.toLowerCase().contains(query)) results.add(ee);
    }

    @Override
    protected boolean addResult(String ee, int index, int cachedWidth) {
        if (ee == null) {
            return false;
        }

        this.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, index * 16, cachedWidth, 16, 0), btnId, ee, ee));

        return true;
    }
}
