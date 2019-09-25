package betterquesting.client.ui_builder;

import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import net.minecraft.client.gui.GuiScreen;

import java.util.HashMap;

public class GuiBuilderMain extends GuiScreenCanvas implements IVolatileScreen
{
    private final HashMap<ComponentPanel, IGuiPanel> PANEL_MAP = new HashMap<>();
    
    public GuiBuilderMain(GuiScreen parent)
    {
        super(parent);
    }
}
