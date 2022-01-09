package betterquesting.client.gui2.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.placeholders.ItemPlaceholder;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.questing.tasks.TaskAdvancement;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextFormatting;

public class PanelTaskAdvancement extends CanvasEmpty
{
    private final TaskAdvancement task;
    
    public PanelTaskAdvancement(IGuiRect rect, TaskAdvancement task)
    {
        super(rect);
        this.task = task;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        boolean isComplete = task.isComplete(QuestingAPI.getQuestingUUID(player));
        
        String title = "" + task.advID;
        String desc = "?";
        BigItemStack icon = new BigItemStack(ItemPlaceholder.placeholder);
        
        Advancement adv = task.advID == null ? null : player.connection.getAdvancementManager().getAdvancementList().getAdvancement(task.advID);
        
        if(adv != null && adv.getDisplay() != null)
        {
            title = adv.getDisplay().getTitle().getFormattedText();
            desc = adv.getDisplay().getDescription().getFormattedText();
            icon = new BigItemStack(adv.getDisplay().getIcon());
        }
        
        this.addPanel(new PanelGeneric(new GuiRectangle(0, 0, 24, 24, 0), PresetTexture.ITEM_FRAME.getTexture()));
        this.addPanel(new PanelGeneric(new GuiRectangle(0, 0, 24, 24, -1), new ItemTexture(icon)));
        
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(28, 2, 0, -12), 0), title).setColor(PresetColor.TEXT_MAIN.getColor()));
        String s = isComplete ? (TextFormatting.GREEN.toString() + QuestTranslation.translate("betterquesting.tooltip.complete")) : (TextFormatting.RED.toString() + QuestTranslation.translate("betterquesting.tooltip.incomplete"));
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(28, 14, 0, -24), 0), s).setColor(PresetColor.TEXT_MAIN.getColor()));
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 32, 0, 0), 0), desc).setColor(PresetColor.TEXT_MAIN.getColor()));
    }
}
