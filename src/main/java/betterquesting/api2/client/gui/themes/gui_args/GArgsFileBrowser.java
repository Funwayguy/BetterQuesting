package betterquesting.api2.client.gui.themes.gui_args;

import betterquesting.api.misc.ICallback;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileFilter;

public class GArgsFileBrowser extends GArgsNone {
    public final File root;
    public final ICallback<File[]> callback;
    public final FileFilter filter;
    public final boolean multiSelect;

    public GArgsFileBrowser(@Nullable GuiScreen parent, File root, ICallback<File[]> callback, FileFilter filter, boolean multiSelect) {
        super(parent);
        this.root = root;
        this.callback = callback;
        this.filter = filter;
        this.multiSelect = multiSelect;
    }
}
