package betterquesting.client.gui2;

import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class GuiQuestLines extends GuiScreenCanvas implements IPEventListener
{
    public GuiQuestLines(GuiScreen parent)
    {
        super(parent);
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
    
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
    }
    
    @Override
    public void onPanelEvent(PanelEvent event)
    {
        if(event == null)
        {
            return;
        } else if(event instanceof PEventButton)
        {
            onButtonPress((PEventButton)event);
        }
    }
    
    private void onButtonPress(PEventButton event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        IPanelButton btn = event.getButton();
    }
}
