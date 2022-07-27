package betterquesting.api2.client.gui.themes;

import javax.annotation.Nonnull;
import net.minecraft.util.ResourceLocation;

// This class is mostly for convenience and including type info to the ID
public class GuiKey<T> {
    private final ResourceLocation ID;

    public GuiKey(@Nonnull ResourceLocation id) {
        this.ID = id;
    }

    @Nonnull
    public ResourceLocation getID() {
        return this.ID;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GuiKey && ((GuiKey) obj).ID.equals(ID);
    }
}
