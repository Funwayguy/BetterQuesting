package betterquesting.api2.client.gui.themes;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

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
