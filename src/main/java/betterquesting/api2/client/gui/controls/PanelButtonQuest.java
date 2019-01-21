package betterquesting.api2.client.gui.controls;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.GuiRectangle;

public class PanelButtonQuest extends PanelButtonStorage<IQuest>
{
    public final GuiRectangle rect;
    
    public PanelButtonQuest(GuiRectangle rect, int id, String txt, IQuest value)
    {
        super(rect, id, txt, value);
        this.rect = rect;
    }
}
