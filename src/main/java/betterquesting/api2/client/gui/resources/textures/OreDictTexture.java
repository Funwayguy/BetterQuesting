package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api.utils.BigItemStack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OreDictTexture extends SlideShowTexture
{
    public OreDictTexture(float interval, BigItemStack stack, boolean showCount, boolean keepAspect)
    {
        super(interval, splitOreTextures(stack, showCount, keepAspect).toArray(new ItemTexture[0]));
    }
    
    public OreDictTexture(float interval, Collection<BigItemStack> list, boolean showCount, boolean keepAspect)
    {
        super(interval, buildTextures(list, showCount, keepAspect).toArray(new ItemTexture[0]));
    }
    
    private static List<ItemTexture> buildTextures(Collection<BigItemStack> subItems, boolean showCount, boolean keepAspect)
    {
        List<ItemTexture> list = new ArrayList<>();
        subItems.forEach((is) -> list.add(new ItemTexture(is, showCount, keepAspect)));
        return list;
    }
    
    private static List<ItemTexture> splitOreTextures(BigItemStack stack, boolean showCount, boolean keepAspect)
    {
        List<ItemTexture> list = new ArrayList<>();
        
        if(!stack.hasOreDict())
        {
            if(stack.getBaseStack().getItemDamage() == OreDictionary.WILDCARD_VALUE)
            {
                List<ItemStack> subItems = new ArrayList<>();
                stack.getBaseStack().getItem().getSubItems(stack.getBaseStack().getItem(), CreativeTabs.tabAllSearch, subItems);
                subItems.forEach((is) -> {
                    BigItemStack bis = new BigItemStack(is);
                    bis.stackSize = stack.stackSize;
                    list.add(new ItemTexture(bis, showCount, keepAspect));
                });
            } else
            {
                list.add(new ItemTexture(stack));
            }
            return list;
        }
        
        for(ItemStack iStack : stack.getOreIngredient().getMatchingStacks())
        {
            if(iStack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
            {
                List<ItemStack> subItems = new ArrayList<>();
                iStack.getItem().getSubItems(iStack.getItem(), CreativeTabs.tabAllSearch, subItems);
                
                for(ItemStack sStack : subItems)
                {
                    BigItemStack bStack = new BigItemStack(sStack);
                    bStack.stackSize = stack.stackSize;
                    list.add(new ItemTexture(bStack, showCount, keepAspect));
                }
            } else
            {
                BigItemStack bStack = new BigItemStack(iStack);
                bStack.stackSize = stack.stackSize;
                list.add(new ItemTexture(bStack, showCount, keepAspect));
            }
        }
        
        return list;
    }
}
