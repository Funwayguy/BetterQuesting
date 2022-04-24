package betterquesting.api2.client.gui;

import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;

import javax.annotation.Nonnull;

// The root canvas in charge of extra top level functions. Should idealy be attached to a GuiScreen but left open for other embedded use cases
// Inner panels can make use of the fact that this is an IGuiCanvas to search through the whole heirachy of panel content on screen
public interface IScene extends IGuiCanvas {
    // Unadjusted canvas representing the entire screen's bounds
    //IGuiCanvas getRootCanvas();

    // TODO: Force isolate all UI interaction to this panel until manually unfocused. NOTE: Escape key will always unfocus to prevent softlocking
    //void forceFocus(@Nonnull IGuiPanel panel);
    //void resetFocus();

    // Opens a top level canvas off the root canvas
    void openPopup(@Nonnull IGuiPanel panel);

    void closePopup();
}
