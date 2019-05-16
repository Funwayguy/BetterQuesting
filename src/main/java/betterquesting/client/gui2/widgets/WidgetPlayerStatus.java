package betterquesting.client.gui2.widgets;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;

public class WidgetPlayerStatus extends CanvasEmpty
{
    @Nullable
    private EntityPlayer player;
    
    public WidgetPlayerStatus(IGuiRect rect, @Nullable EntityPlayer player)
    {
        super(rect);
        this.player = player;
    }
    
    public WidgetPlayerStatus setPlayer(@Nullable EntityPlayer player)
    {
        this.player = player;
        return this;
    }
    
    @Override
    public void initPanel()
    {
        this.initPanel();
    }
}
