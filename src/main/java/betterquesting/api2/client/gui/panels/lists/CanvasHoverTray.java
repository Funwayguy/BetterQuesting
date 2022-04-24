package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasResizeable;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;

public class CanvasHoverTray extends CanvasResizeable {
    private boolean manualOpen = false;

    private final IGuiRect rectClosed;
    private final IGuiRect rectOpen;

    // Note: You can still attach things to the base canvas instead of these if they don't need to move/switch
    private final IGuiCanvas cvOpen;
    private final IGuiCanvas cvClosed;

    private Runnable actionOpen;
    private Runnable actionClose;

    public CanvasHoverTray(IGuiRect rectClosed, IGuiRect rectOpen, IGuiTexture texture) {
        super(rectClosed, texture);

        this.rectClosed = rectClosed;
        this.rectOpen = rectOpen;

        GuiTransform trans = new GuiTransform(GuiAlign.FULL_BOX);
        trans.setParent(this.getRectLerp());
        cvOpen = new CanvasEmpty(trans);
        cvClosed = new CanvasEmpty(trans);
    }

    public IGuiCanvas getCanvasOpen() {
        return cvOpen;
    }

    public IGuiCanvas getCanvasClosed() {
        return cvClosed;
    }

    public boolean isTrayOpen() {
        return cvOpen.isEnabled();
    }

    public CanvasHoverTray setManualOpen(boolean state) {
        this.manualOpen = state;
        return this;
    }

    public CanvasHoverTray setCloseAction(Runnable action) {
        this.actionClose = action;
        return this;
    }

    public CanvasHoverTray setOpenAction(Runnable action) {
        this.actionOpen = action;
        return this;
    }

    public void setTrayState(boolean open, long time) {
        if (!open && isTrayOpen()) {
            this.lerpToRect(rectClosed, time, true);
            cvOpen.setEnabled(false);
            cvClosed.setEnabled(true);
            if (actionClose != null) actionClose.run();
        } else if (open && !isTrayOpen()) {
            this.lerpToRect(rectOpen, time, true);
            cvOpen.setEnabled(true);
            cvClosed.setEnabled(false);
            if (actionOpen != null) actionOpen.run();
        }
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        if (!manualOpen) {
            if (isTrayOpen() && !rectOpen.contains(mx, my)) {
                setTrayState(false, 200);
            } else if (!isTrayOpen() && rectClosed.contains(mx, my)) {
                setTrayState(true, 200);
            }
        }

        super.drawPanel(mx, my, partialTick);
    }

    @Override
    public void resetCanvas() {
        super.resetCanvas();

        cvOpen.resetCanvas();
        cvClosed.resetCanvas();

        this.addPanel(cvOpen);
        this.addPanel(cvClosed);
        cvOpen.setEnabled(false);
        cvClosed.setEnabled(true);
    }

    @Override
    public void initPanel() {
        super.initPanel();

        this.addPanel(cvOpen);
        this.addPanel(cvClosed);
        cvOpen.setEnabled(false);
        cvClosed.setEnabled(true);

        // May cause some issues elsewhere but for now this is fine
        rectOpen.setParent(rectClosed.getParent());
    }
}
