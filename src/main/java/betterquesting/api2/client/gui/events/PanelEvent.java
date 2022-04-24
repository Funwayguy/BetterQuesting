package betterquesting.api2.client.gui.events;

public abstract class PanelEvent {
    private boolean cancelled = false;

    public abstract boolean canCancel();

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean state) {
        if (!this.canCancel()) {
            throw new IllegalStateException("Attempted to cancel a non cancellable panel event");
        }

        this.cancelled = state;
    }
}
