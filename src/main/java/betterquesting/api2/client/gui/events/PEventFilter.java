package betterquesting.api2.client.gui.events;

/**
 * Utility for safely casting events and their sub-types
 */
public class PEventFilter<T extends PanelEvent> {
    private final Class<T> type;

    public PEventFilter(Class<T> type) {
        this.type = type;
    }

    public Class<T> getType() {
        return this.type;
    }

    public boolean isCompatible(PanelEvent event) {
        return event != null && type.isAssignableFrom(event.getClass());
    }

    /**
     * Safely casts to this filters type or returns null if incompatible
     */
    @SuppressWarnings("unchecked")
    public T castEvent(PanelEvent event) {
        if (!isCompatible(event)) {
            return null;
        }

        return (T) event;
    }
}
