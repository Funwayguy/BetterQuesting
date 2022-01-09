package betterquesting.client.gui2.rewards;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.questing.rewards.RewardRecipe;
import net.minecraft.init.Blocks;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector4f;

public class PanelRewardRecipe extends CanvasEmpty
{
    private final RewardRecipe reward;
    
    public PanelRewardRecipe(IGuiRect rect, RewardRecipe reward)
    {
        super(rect);
        this.reward = reward;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        super.initPanel();
    
        CanvasScrolling cvList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(40, 0, 8, 0), 0));
        this.addPanel(cvList);
    
        PanelVScrollBar scList = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 0, 0, 0), 0));
        this.addPanel(scList);
        cvList.setScrollDriverY(scList);
        
        this.addPanel(new PanelGeneric(new GuiTransform(new Vector4f(0F, 0.5F, 0F, 0.5F), 0, -16, 32, 32, 0), new ItemTexture(new BigItemStack(Blocks.CRAFTING_TABLE))));
        
        int dynamic = 0;
        int entry = 0;
        
        int listWidth = cvList.getTransform().getWidth();
        int iconSize = 24;
        int rowSize = listWidth / iconSize;
        for(String s : reward.recipeNames.split("\n"))
        {
            IRecipe rec = CraftingManager.getRecipe(new ResourceLocation(s));
            if(rec == null) continue;
            if(rec.getRecipeOutput().isEmpty())
            {
                dynamic++;
                continue;
            }
            
            int x = entry % rowSize;
            int y = entry / rowSize;
            entry++;
            
            BigItemStack stack = new BigItemStack(rec.getRecipeOutput());
            PanelItemSlot is = new PanelItemSlot(new GuiRectangle(x * iconSize, y * iconSize, iconSize, iconSize, 0), -1, stack, true);
            cvList.addPanel(is);
        }
        
        if(dynamic > 0) cvList.addPanel(new PanelTextBox(new GuiRectangle(0, (entry / rowSize) * iconSize + 4, listWidth, 14, 0), "+" + dynamic + " more...").setColor(PresetColor.TEXT_MAIN.getColor()));
        if(cvList.getScrollBounds().getHeight() <= 0) scList.setEnabled(false);
    }
}
