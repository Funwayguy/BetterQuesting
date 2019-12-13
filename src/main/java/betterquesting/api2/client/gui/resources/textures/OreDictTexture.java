package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api.utils.BigItemStack;
import net.minecraft.item.Item;

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
        
        if(!stack.hasOreDict())
        {
            list.add(new ItemTexture(stack));
            return list;
        }
        
        for(Item iStack : stack.getOreIngredient().getMatchingItems())
        {
            BigItemStack bStack = new BigItemStack(iStack);
            bStack.stackSize = stack.stackSize;
            list.add(new ItemTexture(bStack, showCount, keepAspect));
        }
        
        return list;
    }
}
