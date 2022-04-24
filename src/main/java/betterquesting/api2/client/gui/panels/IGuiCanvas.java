package betterquesting.api2.client.gui.panels;

import javax.annotation.Nonnull;
import java.util.List;

public interface IGuiCanvas extends IGuiPanel {
    void addPanel(IGuiPanel panel);

    boolean removePanel(IGuiPanel panel);

    @Nonnull
    List<IGuiPanel> getChildren();

    /**
     * Removes all children and resets the canvas to its initial blank state
     */
    void resetCanvas();
}