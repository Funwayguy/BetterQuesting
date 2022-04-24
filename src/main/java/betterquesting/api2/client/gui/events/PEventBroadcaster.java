package betterquesting.api2.client.gui.events;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

/*
    Provides a means of broadcasting various things to and around the currently open GUI.
    Useful if your panel/canvas is contained within a screen you're not in control of but still needs to respond to events
 */
public class PEventBroadcaster {
    public static PEventBroadcaster INSTANCE = new PEventBroadcaster();

    private final HashMap<Class<? extends PanelEvent>, PEventEntry<? extends PanelEvent>> entryList = new HashMap<>();

    @Deprecated
    public void register(@Nonnull IPEventListener l, @Nonnull Class<? extends PanelEvent> type) {
        register((Consumer<PanelEvent>) l::onPanelEvent, type);
    }

    public void register(@Nonnull Consumer<PanelEvent> consumer, @Nonnull Class<? extends PanelEvent> type) {
        PEventEntry<?> pe = entryList.computeIfAbsent(type, PEventEntry::new);
        pe.registerListener(consumer);
    }

    public void register(@Nonnull Consumer<PanelEvent> consumer, @Nonnull Iterable<Class<? extends PanelEvent>> type) {
        type.forEach((c) -> {
            PEventEntry<?> pe = entryList.computeIfAbsent(c, PEventEntry::new);
            pe.registerListener(consumer);
        });
    }

    @Deprecated
    public void unregister(IPEventListener l) {
        unregister((Consumer<PanelEvent>) l::onPanelEvent);
    }

    public void unregister(@Nonnull Consumer<PanelEvent> consumer) {
        entryList.values().forEach((value) -> value.unregisterListener(consumer));
    }

    public boolean postEvent(@Nonnull PanelEvent event) {
        // We cycle over all entries incase we need to fire events for parent class types
        for (Entry<Class<? extends PanelEvent>, PEventEntry<? extends PanelEvent>> e : entryList.entrySet()) {
            if (!e.getKey().isAssignableFrom(event.getClass())) continue;
            e.getValue().fire(event);
        }

        return event.canCancel() && event.isCancelled();
    }

    /**
     * Clears event listeners whenever a new GUI loads. If you must have cross GUI communication either handle this yourself or re-register the relevant listeners.
     */
    @SubscribeEvent
    public void onGuiOpened(GuiOpenEvent event) {
        entryList.clear();
    }
}
