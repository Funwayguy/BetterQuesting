package betterquesting.api2.client.gui.panels.content;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.ColorTexture;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.resources.textures.LayeredTexture;
import betterquesting.api2.client.gui.resources.textures.OreDictTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;

import java.util.List;

public class PanelItemSlot extends PanelButtonStorage<BigItemStack>
{
    private final boolean showCount;
    private final boolean oreDict;
    
    public PanelItemSlot(IGuiRect rect, int id, BigItemStack value)
    {
        this(rect, id, value, false, false);
    }
    
    public PanelItemSlot(IGuiRect rect, int id, BigItemStack value, boolean showCount)
    {
        this(rect, id, value, showCount, false);
    }
    
    public PanelItemSlot(IGuiRect rect, int id, BigItemStack value, boolean showCount, boolean oreDict)
    {
        super(rect, id, "", value);
        this.showCount = showCount;
        this.oreDict = oreDict;
        
        this.setTextures(PresetTexture.ITEM_FRAME.getTexture(), PresetTexture.ITEM_FRAME.getTexture(), new LayeredTexture(PresetTexture.ITEM_FRAME.getTexture(), new ColorTexture(PresetColor.ITEM_HIGHLIGHT.getColor(), new GuiPadding(1, 1, 1, 1))));
        this.setStoredValue(value); // Need to run this again because of the instatiation order of showCount
    
    }
    
    @Override
    public PanelItemSlot setStoredValue(BigItemStack value)
    {
        super.setStoredValue(value);
        
        if(value != null)
        {
            Minecraft mc = Minecraft.getMinecraft();
            this.setIcon(oreDict ? new OreDictTexture(1F, value, showCount, true) : new ItemTexture(value, showCount, true), 1);
            this.setTooltip(value.getBaseStack().getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
        } else
        {
            this.setIcon(null);
            this.setTooltip(null);
        }
        
        return this;
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        if(getStoredValue() != null && getTransform().contains(mx, my))
        {
            Minecraft mc = Minecraft.getMinecraft();
            return getStoredValue().getBaseStack().getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);
        }
        
        return null;
    }
}
