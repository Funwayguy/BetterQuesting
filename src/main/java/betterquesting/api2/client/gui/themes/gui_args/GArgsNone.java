package betterquesting.api2.client.gui.themes.gui_args;

import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;

public class GArgsNone {
    public static final GArgsNone NONE = new GArgsNone(null);

    @Nullable
    public final GuiScreen parent;

    public GArgsNone(@Nullable GuiScreen parent) {
        this.parent = parent;
    }
}
