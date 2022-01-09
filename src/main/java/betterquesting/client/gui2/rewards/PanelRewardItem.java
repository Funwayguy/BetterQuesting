package betterquesting.client.gui2.rewards;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.questing.rewards.RewardItem;

public class PanelRewardItem extends CanvasEmpty
{
    private final RewardItem reward;
    
    public PanelRewardItem(IGuiRect rect, RewardItem reward)
    {
        super(rect);
        this.reward = reward;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
    
        CanvasScrolling cvList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 8, 0), 0));
        this.addPanel(cvList);
    
        PanelVScrollBar scList = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 0, 0, 0), 0));
        this.addPanel(scList);
        cvList.setScrollDriverY(scList);
        
        int listWidth = cvList.getTransform().getWidth();
        for(int i = 0; i < reward.items.size(); i++)
        {
            BigItemStack stack = reward.items.get(i);
            PanelItemSlot is = new PanelItemSlot(new GuiRectangle(0, i * 18, 18, 18, 0), -1, stack, true);
            cvList.addPanel(is);
            
            cvList.addPanel(new PanelTextBox(new GuiRectangle(22, i * 18 + 4, listWidth - 22, 14, 0), stack.stackSize + " " + stack.getBaseStack().getDisplayName()).setColor(PresetColor.TEXT_MAIN.getColor()));
        }
    }
}
