package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayDeque;
import java.util.Iterator;

public class CanvasItemDatabase extends CanvasSearch<ItemStack, Item>
{
    private final int btnId;
    
    public CanvasItemDatabase(IGuiRect rect, int buttonId)
    {
        super(rect);
        
        this.btnId = buttonId;
    }
    
    @Override
    protected Iterator<Item> getIterator()
    {
        return ForgeRegistries.ITEMS.iterator();
    }
    
    @Override
    protected void queryMatches(Item item, String query, final ArrayDeque<ItemStack> results)
    {
        if(item == null || item.getRegistryName() == null)
        {
            return;
        } else if(item == Items.AIR)
        {
            results.add(ItemStack.EMPTY);
            return;
        }
        
        try
        {
            NonNullList<ItemStack> subList = NonNullList.create();
            
            item.fillItemGroup(ItemGroup.SEARCH, subList);
            
            if(subList.isEmpty())
            {
                subList.add(item.getDefaultInstance());
            }
            
            if(item.getTranslationKey().toLowerCase().contains(query) || QuestTranslation.translate(item.getTranslationKey()).toLowerCase().contains(query) || item.getRegistryName().toString().toLowerCase().contains(query))
            {
                results.addAll(subList);
            } else
            {
                for(ItemStack subItem : subList)
                {
                    try
                    {
                        if(subItem.getTranslationKey().toLowerCase().contains(query) || subItem.getDisplayName().getFormattedText().toLowerCase().contains(query))
                        {
                            results.add(subItem);
                            continue;
                        }
                        
                        /*int[] oids = OreDictionary.getOreIDs(subItem);
                        for(int oid : oids)
                        {
                            if(OreDictionary.getOreName(oid).toLowerCase().contains(query))
                            {
                                results.add(subItem);
                                break;
                            }
                        }*/
                    } catch(Exception e)
                    {
                        BetterQuesting.logger.error("An error occured while searching itemstack " + subItem.toString() + " from item \"" + item.getRegistryName() + "\" (" + item.getClass().getName() + ").\nNBT: " + subItem.write(new CompoundNBT()), e);
                    }
                }
            }
        } catch(Exception e)
        {
            BetterQuesting.logger.error("An error occured while searching item \"" + item.getRegistryName() + "\" (" + item.getClass().getName() + ")", e);
        }
    }
    
    @Override
    public boolean addResult(ItemStack stack, int index, int cachedWidth)
    {
        if(stack == null) return false;
        
        int x = (index % (cachedWidth / 18)) * 18;
        int y = (index / (cachedWidth / 18)) * 18;
        
        this.addPanel(new PanelItemSlot(new GuiRectangle(x, y, 18, 18, 0), btnId, new BigItemStack(stack)));
        
        return true;
    }
}
