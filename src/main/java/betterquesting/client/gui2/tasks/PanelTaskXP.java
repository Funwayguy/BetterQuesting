package betterquesting.client.gui2.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.controls.io.ValueFuncIO;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.bars.PanelHBarFill;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.XPHelper;
import betterquesting.questing.tasks.TaskXP;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.util.vector.Vector4f;

public class PanelTaskXP extends CanvasEmpty
{
    private final TaskXP task;
    
    public PanelTaskXP(IGuiRect rect, TaskXP task)
    {
        super(rect);
        this.task = task;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, -16, -32, 32, 32, 0), new ItemTexture(new BigItemStack(Items.EXPERIENCE_BOTTLE))));
        
		long xp = task.getUsersProgress(QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player));
		xp = !task.levels? xp : XPHelper.getXPLevel(xp);
		final float xpPercent = (float)((double)xp/(double)task.amount);
        
        PanelHBarFill fillBar = new PanelHBarFill(new GuiTransform(new Vector4f(0.25F, 0.5F, 0.75F, 0.5F), new GuiPadding(0, 0, 0, -16), 0));
        fillBar.setFillColor(new GuiColorStatic(0xFF00FF00));
        fillBar.setFillDriver(new ValueFuncIO<>(() -> xpPercent));
        this.addPanel(fillBar);
        
        this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0.25F, 0.5F, 0.75F, 0.5F), new GuiPadding(0, 4, 0, -16), -1), TextFormatting.BOLD + "" + xp + "/" + task.amount + (task.levels ? "L" : "XP")).setAlignment(1));
    }
}
