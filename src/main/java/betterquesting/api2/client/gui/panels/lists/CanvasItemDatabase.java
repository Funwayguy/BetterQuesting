package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public class CanvasItemDatabase extends CanvasSearch<ItemStack, Item>
{
    private final int btnId;
    
    public CanvasItemDatabase(IGuiRect rect, int buttonId)
    {
        super(rect);
        
        this.btnId = buttonId;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected Iterator<Item> getIterator()
    {
        return (Iterator<Item>)Item.itemRegistry.iterator();
    }
    
    @Override
    protected void queryMatches(Item item, String query, final ArrayDeque<ItemStack> results)
    {
        if(item == null) return;
        
        String regName = Item.itemRegistry.getNameForObject(item); // This is gonna be really slow but... meh. It's rate limited in the parent class
        if(regName == null) return;
        
        try
        {
            final List<ItemStack> subList = new ArrayList<>();
            
            item.getSubItems(item, CreativeTabs.tabAllSearch, subList);
            if(subList.isEmpty()) subList.add(new ItemStack(item));
            
            if(regName.toLowerCase().contains(query) || item.getUnlocalizedName().toLowerCase().contains(query) || QuestTranslation.translate(item.getUnlocalizedName()).toLowerCase().contains(query))
            {
                results.addAll(subList);
                return;
            }
            
            subList.parallelStream().forEach((subItem) -> {
                try
                {
                    if(subItem.getUnlocalizedName().toLowerCase().contains(query) || subItem.getDisplayName().toLowerCase().contains(query))
                    {
                        results.add(subItem);
                    } else if(Arrays.stream(OreDictionary.getOreIDs(subItem)).anyMatch((id) -> OreDictionary.getOreName(id).toLowerCase().contains(query)))
                    {
                        results.add(subItem);
                    }
                } catch(Exception e)
                {
                    BetterQuesting.logger.error("An error occured while searching itemstack " + subItem.toString() + " from item \"" + regName + "\" (" + item.getClass().getName() + ").\nNBT: " + subItem.writeToNBT(new NBTTagCompound()), e);
                }
            });
        } catch(Exception e)
        {
            BetterQuesting.logger.error("An error occured while searching item \"" + regName + "\" (" + item.getClass().getName() + ")", e);
        }
    }
    
    @Override
    public boolean addResult(ItemStack stack, int index, int cachedWidth)
    {
        if(stack == null || stack.getItem() == null) return false;
        
        int x = (index % (cachedWidth / 18)) * 18;
        int y = (index / (cachedWidth / 18)) * 18;
        
        this.addPanel(new PanelItemSlot(new GuiRectangle(x, y, 18, 18, 0), btnId, new BigItemStack(stack)));
        
        return true;
    }
}
