package betterquesting.api2.client.gui.events;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PEventEntry<T extends PanelEvent> {
    private final List<Consumer<PanelEvent>> listeners = new ArrayList<>();
    private final Class<T> cType;

    public PEventEntry(Class<T> type) {
        this.cType = type;
    }

    public void registerListener(@Nonnull Consumer<PanelEvent> consumer) {
        if (listeners.contains(consumer)) return;
        listeners.add(consumer);
    }

    public void unregisterListener(@Nonnull Consumer<PanelEvent> consumer) {
        listeners.remove(consumer);
    }

    public void fire(@Nonnull PanelEvent event) {
        if (!cType.isAssignableFrom(event.getClass())) return;
        listeners.forEach((l) -> l.accept(event));
    }
}
