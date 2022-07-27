package betterquesting.api2.client.gui.themes.gui_args;

import betterquesting.api.misc.ICallback;
import net.minecraft.client.gui.GuiScreen;

public class GArgsCallback<T> extends GArgsNone {
    public final T value;
    public final ICallback<T> callback;

    public GArgsCallback(GuiScreen parent, T value, ICallback<T> callback) {
        super(parent);
        this.value = value;
        this.callback = callback;
    }
}
