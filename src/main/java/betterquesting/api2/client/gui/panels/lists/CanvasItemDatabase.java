package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import com.google.common.base.Stopwatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class CanvasItemDatabase extends CanvasScrolling
{
    private final int btnId;
    private String searchTerm = "";
    private Iterator<Item> searching = null;
    private final Stopwatch searchTime = Stopwatch.createStarted();
    private int resultWidth = 256; // Used for organising ongoing search results even if the size changes midway
    private int searchIdx = 0; // Where are we in the ongoing search?
    private final ArrayDeque<ItemStack> pendingResults = new ArrayDeque<>();
    
    public CanvasItemDatabase(IGuiRect rect, int buttonId)
    {
        super(rect);
        
        this.btnId = buttonId;
    }
    
    public void setSearchFilter(String text)
    {
        this.resetCanvas();
        
        this.searchTerm = text.toLowerCase();
        this.searchIdx = 0;
        this.searching = Item.REGISTRY.iterator();
        this.resultWidth = this.getTransform().getWidth();
        this.pendingResults.clear();
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        this.searchIdx = 0;
        this.searching = Item.REGISTRY.iterator();
        this.resultWidth = this.getTransform().getWidth();
        this.pendingResults.clear();
    }
    
    @Override
    public void drawPanel(int mx, int my, float partialTick)
    {
        updateSearch();
        updateResults();
        
        super.drawPanel(mx, my, partialTick);
    }
    
    private void updateSearch()
    {
        if(searching == null)
        {
            return;
        } else if(!searching.hasNext())
        {
            searching = null;
            return;
        }
    
        Minecraft mc = Minecraft.getMinecraft();
        
        searchTime.reset().start();
        
        while(searching.hasNext() && searchTime.elapsed(TimeUnit.MILLISECONDS) < 10)
        {
            Item item = searching.next();
            
            if(item == null || item == Items.AIR || item.getRegistryName() == null)
            {
                continue;
            }
            
            try
            {
                NonNullList<ItemStack> subList = NonNullList.create();
                
                item.getSubItems(CreativeTabs.SEARCH, subList);
                
                if(subList.isEmpty())
                {
                    subList.add(item.getDefaultInstance());
                }
                
                if(item.getUnlocalizedName().toLowerCase().contains(searchTerm) || QuestTranslation.translate(item.getUnlocalizedName()).toLowerCase().contains(searchTerm) || item.getRegistryName().toString().toLowerCase().contains(searchTerm))
                {
                    pendingResults.addAll(subList);
                } else
                {
                    for(ItemStack subItem : subList)
                    {
                        try
                        {
                            if(subItem.getUnlocalizedName().toLowerCase().contains(searchTerm) || subItem.getDisplayName().toLowerCase().contains(searchTerm))
                            {
                                pendingResults.add(subItem);
                                continue;
                            }
    
                            boolean match = false;
    
                            for(String s : subItem.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL))
                            {
                                if(s.toLowerCase().contains(searchTerm))
                                {
                                    pendingResults.add(subItem);
                                    match = true;
                                    break;
                                }
                            }
    
                            int[] oids = OreDictionary.getOreIDs(subItem);
                            for(int i = 0; i < oids.length && !match; i++)
                            {
                                if(OreDictionary.getOreName(oids[i]).toLowerCase().contains(searchTerm))
                                {
                                    pendingResults.add(subItem);
                                    break;
                                }
                            }
                        } catch(Exception e)
                        {
                            BetterQuesting.logger.error("An error occured while searching itemstack " + subItem.toString() + " from item \"" + item.getRegistryName() + "\" (" + item.getClass().getName() + ").\nNBT: " + subItem.writeToNBT(new NBTTagCompound()), e);
                        }
                    }
                }
            } catch(Exception e)
            {
                BetterQuesting.logger.error("An error occured while searching item \"" + item.getRegistryName() + "\" (" + item.getClass().getName() + ")", e);
            }
        }
        
        searchTime.stop();
    }
    
    private void updateResults()
    {
        if(pendingResults.isEmpty())
        {
            return;
        }
        
        int rowMax = resultWidth / 18;
        
        searchTime.reset().start();
        
        while(!pendingResults.isEmpty() && searchTime.elapsed(TimeUnit.MILLISECONDS) < 100)
        {
            ItemStack stack = pendingResults.poll();
            
            if(stack == null || stack.isEmpty())
            {
                continue;
            }
            
            int x = (searchIdx % rowMax) * 18;
            int y = (searchIdx / rowMax) * 18;
            
            this.addPanel(new PanelItemSlot(new GuiRectangle(x, y, 18, 18, 0), btnId, new BigItemStack(stack)));
            
            searchIdx++;
        }
        
        searchTime.stop();
    }
}
