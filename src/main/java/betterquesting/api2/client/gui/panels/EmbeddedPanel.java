package betterquesting.api2.client.gui.panels;

import betterquesting.api.client.gui.misc.IGuiEmbedded;

@Deprecated
public class EmbeddedPanel implements IGuiEmbedded
{
    private final IGuiPanel panel;
    
    public EmbeddedPanel(IGuiPanel panel)
    {
        this.panel = panel;
    }
    
    @Override
    public void drawBackground(int mx, int my, float partialTick)
    {
        this.panel.drawPanel(mx, my, partialTick);
    }
    
    @Override
    public void drawForeground(int mx, int my, float partialTick)
    {
    }
    
    @Override
    public void onMouseClick(int mx, int my, int click)
    {
        this.panel.onMouseClick(mx, my, click);
    }
    
    @Override
    public void onMouseScroll(int mx, int my, int scroll)
    {
        this.panel.onMouseScroll(mx, my, scroll);
    }
    
    @Override
    public void onKeyTyped(char c, int keyCode)
    {
        this.panel.onKeyTyped(c, keyCode);
    }
}
