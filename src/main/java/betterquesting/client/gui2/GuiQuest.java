package betterquesting.client.gui2;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiQuest extends GuiScreenCanvas implements IPEventListener
{
    private final int questID;
    
    private IQuest quest;
    
    public GuiQuest(GuiScreen parent, int questID)
    {
        super(parent);
        this.questID = questID;
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
        if(event instanceof PEventButton)
        {
            onButtonPress((PEventButton)event);
        }
    }
    
    private void onButtonPress(PEventButton event)
    {
    
    }
}
