package betterquesting.client.gui2;

import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;

/**
 * My class for lazy quest line setup on a scrolling canvas
 */
public class CanvasQuestLine extends CanvasScrolling
{
    private final int buttonId;
    
    public CanvasQuestLine(IGuiRect rect, int buttonId)
    {
        super(rect);
        this.enableZoomScroll(true);
        
        this.buttonId = buttonId;
    }
    
    /**
     * Loads in quests and connecting lines
     * @param line The quest line to load
     */
    public void setQuestLine(IQuestLine line)
    {
    
    }
}
