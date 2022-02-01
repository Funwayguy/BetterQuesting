package betterquesting.client.gui2.rewards;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasMinimum;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.handlers.NetRewardChoice;
import betterquesting.questing.rewards.RewardChoice;
import net.minecraft.client.Minecraft;
import org.lwjgl.util.vector.Vector4f;

import java.util.UUID;

public class PanelRewardChoice extends CanvasMinimum {

    private final DBEntry<IQuest> quest;
    private final RewardChoice reward;
    private final IGuiRect initialRect;

    public PanelRewardChoice(IGuiRect rect, DBEntry<IQuest> quest, RewardChoice reward) {
        super(rect);
        this.initialRect = rect;
        this.quest = quest;
        this.reward = reward;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        UUID uuid = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
        int sel = reward.getSelecton(uuid);
        PanelItemSlot slot = new PanelItemSlot(new GuiTransform(new Vector4f(0F, 0F, 0F, 0F), 0, 0, 32, 32, 0), -1, sel < 0 ? null : reward.choices.get(sel));
        this.addPanel(slot);

        final int qID = quest.getID();
        final int rID = quest.getValue().getRewards().getID(reward);

        int listWidth = initialRect.getWidth();
        for (int i = 0; i < reward.choices.size(); i++) {
            BigItemStack stack = reward.choices.get(i);
            PanelItemSlot is = new PanelItemSlot(new GuiRectangle(40, i * 18, 18, 18, 0), -1, stack, true);
            this.addPanel(is);

            this.addPanel(new PanelTextBox(new GuiRectangle(62, i * 18 + 4, listWidth - 22, 14, 0), stack.stackSize + " " + stack.getBaseStack().getDisplayName()).setColor(PresetColor.TEXT_MAIN.getColor()));

            final int sID = i;
            is.setCallback(value -> NetRewardChoice.requestChoice(qID, rID, sID));
        }
        recalculateSizes();
    }
}
