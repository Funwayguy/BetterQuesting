package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CanvasItemDatabase extends CanvasSearch<ItemStack, Item>
{
    private final int btnId;
    private final Minecraft mc = Minecraft.getMinecraft();
    
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
    @SuppressWarnings("unchecked")
    protected void queryMatches(Item item, String query, final ArrayDeque<ItemStack> results)
    {
        if(item == null) return;
        
        String regName = Item.itemRegistry.getNameForObject(item); // This is gonna be really slow but... meh. It's rate limited in the parent class
        if(regName == null) return;
        
        try
        {
            List<ItemStack> subList = new ArrayList<>();
            
            item.getSubItems(item, CreativeTabs.tabAllSearch, subList);
            
            if(subList.isEmpty())
            {
                subList.add(new ItemStack(item));
            }
            
            
            
            if(item.getUnlocalizedName().toLowerCase().contains(query) || QuestTranslation.translate(item.getUnlocalizedName()).toLowerCase().contains(query) || regName.toLowerCase().contains(query))
            {
                results.addAll(subList);
            } else
            {
                for(ItemStack subItem : subList)
                {
                    try
                    {
                        if(subItem.getUnlocalizedName().toLowerCase().contains(query) || subItem.getDisplayName().toLowerCase().contains(query))
                        {
                            results.add(subItem);
                            continue;
                        }

                        boolean match = false;

                        for(String s : (List<String>)subItem.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips))
                        {
                            if(s.toLowerCase().contains(query))
                            {
                                results.add(subItem);
                                match = true;
                                break;
                            }
                        }

                        int[] oids = OreDictionary.getOreIDs(subItem);
                        for(int i = 0; i < oids.length && !match; i++)
                        {
                            if(OreDictionary.getOreName(oids[i]).toLowerCase().contains(query))
                            {
                                results.add(subItem);
                                break;
                            }
                        }
                    } catch(Exception e)
                    {
                        BetterQuesting.logger.error("An error occured while searching itemstack " + subItem.toString() + " from item \"" + regName + "\" (" + item.getClass().getName() + ").\nNBT: " + subItem.writeToNBT(new NBTTagCompound()), e);
                    }
                }
            }
        } catch(Exception e)
        {
            BetterQuesting.logger.error("An error occured while searching item \"" + regName + "\" (" + item.getClass().getName() + ")", e);
        }
    }
    
    @Override
    public boolean addResult(ItemStack stack, int index, int cachedWidth)
    {
        if(stack == null)
        {
            return false;
        }
        
        int x = (index % (cachedWidth / 18)) * 18;
        int y = (index / (cachedWidth / 18)) * 18;
        
        this.addPanel(new PanelItemSlot(new GuiRectangle(x, y, 18, 18, 0), btnId, new BigItemStack(stack)));
        
        return true;
    }
}
