package betterquesting.api.client.toolbox;

import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import net.minecraft.util.NonNullList;

import java.util.List;

public interface IToolboxTool {
    /**
     * Starts up the tool in its initial starting state
     */
    void initTool(CanvasQuestLine gui);

    /**
     * Canvas has been refreshed. Restore references to buttons, etc.
     */
    void refresh(CanvasQuestLine gui);

    /**
     * Shut down tool and reset values
     */
    void disableTool();

    /**
     * Draws within the relative scrolling portion of the canvas
     */
    void drawCanvas(int mx, int my, float partialTick);

    /**
     * Draws over the top of the canvas without being affected by scrolling
     */
    void drawOverlay(int mx, int my, float partialTick);

    /**
     * Fired when the tool controller has changed its multi-selection
     */
    void onSelection(NonNullList<PanelButtonQuest> buttons);

    boolean onMouseClick(int mx, int my, int click);

    boolean onMouseRelease(int mx, int my, int click);

    boolean onMouseScroll(int mx, int my, int scroll);

    boolean onKeyPressed(char c, int key);

    List<String> getTooltip(int mx, int my);

    boolean clampScrolling();

    /**
     * Allows the tool controller to intercept some interactions to perform multi-quest selections
     */
    boolean useSelection();
}
