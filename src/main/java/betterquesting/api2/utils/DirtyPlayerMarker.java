package betterquesting.api2.utils;

import betterquesting.api.events.MarkDirtyPlayerEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collection;
import java.util.UUID;

public class DirtyPlayerMarker {

    public static void markDirty(Collection<UUID> players) {
        MinecraftForge.EVENT_BUS.post(new MarkDirtyPlayerEvent(players));
    }

    public static void markDirty(UUID player) {
        MinecraftForge.EVENT_BUS.post(new MarkDirtyPlayerEvent(player));
    }
}
