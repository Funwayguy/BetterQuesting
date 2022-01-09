package betterquesting.client.gui2.rewards;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.handlers.NetRewardChoice;
import betterquesting.questing.rewards.RewardChoice;
import net.minecraft.client.Minecraft;
import org.lwjgl.util.vector.Vector4f;

import java.util.UUID;

public class PanelRewardChoice extends CanvasEmpty
{
    private final DBEntry<IQuest> quest;
    private final RewardChoice reward;
    
    public PanelRewardChoice(IGuiRect rect, DBEntry<IQuest> quest, RewardChoice reward)
    {
        super(rect);
        this.quest = quest;
        this.reward = reward;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
    
        CanvasScrolling cvList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(40, 0, 8, 0), 0));
        this.addPanel(cvList);
    
        PanelVScrollBar scList = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 0, 0, 0), 0));
        this.addPanel(scList);
        cvList.setScrollDriverY(scList);
        
        UUID uuid = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
        int sel = reward.getSelecton(uuid);
        PanelItemSlot slot = new PanelItemSlot(new GuiTransform(new Vector4f(0F, 0.5F, 0F, 0.5F), 0, -16, 32, 32, 0), -1, sel < 0 ? null : reward.choices.get(sel));
        this.addPanel(slot);
        
        final int qID = quest.getID();
        final int rID = quest.getValue().getRewards().getID(reward);
        
        int listWidth = cvList.getTransform().getWidth();
        for(int i = 0; i < reward.choices.size(); i++)
        {
            BigItemStack stack = reward.choices.get(i);
            PanelItemSlot is = new PanelItemSlot(new GuiRectangle(0, i * 18, 18, 18, 0), -1, stack, true);
            cvList.addPanel(is);
            
            cvList.addPanel(new PanelTextBox(new GuiRectangle(22, i * 18 + 4, listWidth - 22, 14, 0), stack.stackSize + " " + stack.getBaseStack().getDisplayName()).setColor(PresetColor.TEXT_MAIN.getColor()));
            
            final int sID = i;
            is.setCallback(value -> NetRewardChoice.requestChoice(qID, rID, sID));
        }
    }
}
