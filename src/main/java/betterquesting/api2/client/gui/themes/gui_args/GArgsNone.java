package betterquesting.api2.client.gui.themes.gui_args;

import net.minecraft.client.gui.screen.Screen;

import javax.annotation.Nullable;

public class GArgsNone
{
    public static final GArgsNone NONE = new GArgsNone(null);
    
    @Nullable
    public final Screen parent;
    
    public GArgsNone(@Nullable Screen parent)
    {
        this.parent = parent;
    }
}
