package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api.utils.BigItemStack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class OreDictTexture extends SlideShowTexture
{
    public OreDictTexture(float interval, BigItemStack stack, boolean showCount, boolean keepAspect)
    {
        super(interval, splitOreTextures(stack, showCount, keepAspect).toArray(new ItemTexture[0]));
    }
    
    private static List<ItemTexture> splitOreTextures(BigItemStack stack, boolean showCount, boolean keepAspect)
    {
        List<ItemTexture> list = new ArrayList<>();
        
        if(stack.oreDict == null || stack.oreDict.length() <= 0)
        {
            list.add(new ItemTexture(stack));
            return list;
        }
        
        for(ItemStack iStack : OreDictionary.getOres(stack.oreDict))
        {
            if(iStack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
            {
                NonNullList<ItemStack> subItems = NonNullList.create();
                iStack.getItem().getSubItems(CreativeTabs.SEARCH, subItems);
                
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
