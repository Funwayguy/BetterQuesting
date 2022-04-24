package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api2.client.gui.misc.ComparatorGuiDepth;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class CanvasCullingManager {
    private final List<IGuiPanel> dynamicPanels = new ArrayList<>(); // Panels not regioned (likely moving or important)

    private final Map<String, RegionInfo> panelRegions = new HashMap<>(); // Panels separated into regional blocks

    private final List<IGuiPanel> cachedPanels = new CopyOnWriteArrayList<>(); // The last updated list of visible panels

    private final int gridSize;

    public CanvasCullingManager() {
        this(128);
    }

    public CanvasCullingManager(int gridSize) {
        this.gridSize = gridSize;
    }

    public void reset() {
        dynamicPanels.clear();
        panelRegions.clear();
        cachedPanels.clear();
    }

    public void addPanel(IGuiPanel panel, boolean useBlocks) {
        if (!useBlocks) {
            if (!dynamicPanels.contains(panel)) {
                dynamicPanels.add(panel);
                cachedPanels.add(panel);
            }
        } else {
            int x = panel.getTransform().getX();
            int y = panel.getTransform().getY();
            x = (x - ((x % gridSize) + gridSize) % gridSize) / gridSize;
            y = (y - ((y % gridSize) + gridSize) % gridSize) / gridSize;

            RegionInfo cbl = panelRegions.get(x + "," + y);

            if (cbl != null) {
                if (!cbl.panels.contains(panel)) {
                    cbl.panels.add(panel);

                    int minX = Math.min(cbl.rect.x, panel.getTransform().getX());
                    int minY = Math.min(cbl.rect.y, panel.getTransform().getY());
                    int maxX = Math.max(cbl.rect.x + cbl.rect.w, panel.getTransform().getX() + panel.getTransform().getWidth());
                    int maxY = Math.max(cbl.rect.y + cbl.rect.h, panel.getTransform().getY() + panel.getTransform().getHeight());

                    cbl.rect.x = minX;
                    cbl.rect.y = minY;
                    cbl.rect.w = maxX - minX;
                    cbl.rect.h = maxY - minY;

                    if (cbl.enabled) {
                        cachedPanels.add(panel);
                        cachedPanels.sort(ComparatorGuiDepth.INSTANCE);
                    }
                }

            } else {
                cbl = new RegionInfo(x, y, gridSize);

                int minX = Math.min(cbl.rect.x, panel.getTransform().getX());
                int minY = Math.min(cbl.rect.y, panel.getTransform().getY());
                int maxX = Math.max(cbl.rect.x + cbl.rect.w, panel.getTransform().getX() + panel.getTransform().getWidth());
                int maxY = Math.max(cbl.rect.y + cbl.rect.h, panel.getTransform().getY() + panel.getTransform().getHeight());

                cbl.rect.x = minX;
                cbl.rect.y = minY;
                cbl.rect.w = maxX - minX;
                cbl.rect.h = maxY - minY;

                cbl.panels.add(panel);
                panelRegions.put(x + "," + y, cbl);
            }
        }
    }

    public void removePanel(IGuiPanel panel) {
        if (!dynamicPanels.remove(panel)) {
            for (RegionInfo cbl : panelRegions.values()) {
                if (cbl.panels.remove(panel)) {
                    cbl.refreshBounds();
                    cachedPanels.remove(panel);
                    break;
                }
            }
        } else {
            cachedPanels.remove(panel);
        }
    }

    public List<IGuiPanel> getVisiblePanels() {
        return cachedPanels;
    }

    public void updateVisiblePanels(IGuiRect region) {
        boolean changed = false;

        for (RegionInfo cb : panelRegions.values()) {
            boolean prevState = cb.enabled;
            cb.enabled = overlapCheck(cb.rect, region);

            if (prevState != cb.enabled) {
                if (cb.enabled) {
                    cachedPanels.addAll(cb.panels);
                } else {
                    cachedPanels.removeAll(cb.panels);
                }

                changed = true;
            }
        }

        if (changed) {
            cachedPanels.sort(ComparatorGuiDepth.INSTANCE);
        }
    }

    // TODO: Move this to a utility class
    private static boolean overlapCheck(IGuiRect rect1, IGuiRect rect2) {
        if (rect1.getX() + rect1.getWidth() < rect2.getX() || rect1.getX() > rect2.getX() + rect2.getWidth()) // Rectangle outside width bounds
        {
            return false;
        } else {
            return rect1.getY() + rect1.getHeight() >= rect2.getY() && rect1.getY() <= rect2.getY() + rect2.getHeight();
        }
    }

    private static class RegionInfo {
        private final List<IGuiPanel> panels = new ArrayList<>();

        private boolean enabled = false; // Starts disabled so that the cache can be populated on initial checks
        private final GuiRectangle rect; // Needs to be updated with the min-max bounds (doesn't actually conform to any grid)

        private RegionInfo(int blockX, int blockY, int gridSize) {
            this.rect = new GuiRectangle(blockX * gridSize, blockY * gridSize, gridSize, gridSize);
        }

        private void refreshBounds() {
            boolean set = false;
            int minX = 0;
            int minY = 0;
            int maxX = 0;
            int maxY = 0;

            for (IGuiPanel pan : panels) {
                if (!set) {
                    set = true;
                    minX = pan.getTransform().getX();
                    minY = pan.getTransform().getY();
                    maxX = minX + pan.getTransform().getWidth();
                    maxY = minY + pan.getTransform().getHeight();
                } else {
                    minX = Math.min(minX, pan.getTransform().getX());
                    minY = Math.min(minY, pan.getTransform().getY());
                    maxX = Math.max(maxX, pan.getTransform().getX() + pan.getTransform().getWidth());
                    maxY = Math.max(maxY, pan.getTransform().getY() + pan.getTransform().getHeight());
                }
            }

            rect.x = minX;
            rect.y = minY;
            rect.w = maxX - minX;
            rect.h = maxY - minY;
        }
    }
}