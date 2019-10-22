package betterquesting.api2.client.gui.popups;

import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.ColorTexture;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;

import javax.annotation.Nullable;

public class PopMessage extends CanvasEmpty
{
    private final String message;
    private final IGuiTexture icon;
    
    public PopMessage(String message)
    {
        this(message, null);
    }
    
    public PopMessage(String message, @Nullable IGuiTexture icon)
    {
        super(new GuiTransform(GuiAlign.MID_CENTER, -128, -32, 256, 128, 0));
        this.message = message;
        this.icon = icon;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        this.addPanel(new PanelGeneric(new GuiTransform(), new ColorTexture(new GuiColorStatic(0x80000000))));
    }
}
