package betterquesting.importers.ftbq;

import betterquesting.api.placeholders.PlaceholderConverter;
import betterquesting.api.utils.BigItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

public class FTBQUtils
{
    public static BigItemStack convertItem(NBTBase tag)
    {
        if(tag instanceof NBTTagString)
        {
            return convertItemType1(((NBTTagString)tag).getString());
        } else if(tag instanceof NBTTagCompound)
        {
            return convertItemType2((NBTTagCompound)tag);
        }
        
        return new BigItemStack(ItemStack.EMPTY);
    }
    
    private static BigItemStack convertItemType1(String string)
    {
        String[] split = string.split(" ");
        if(split.length <= 0) return new BigItemStack(ItemStack.EMPTY);
        
        Item item = Item.REGISTRY.getObject(new ResourceLocation(split[0]));
        int count = split.length < 2 ? 1 : tryParseInt(split[1], 1);
        int meta = split.length < 3 ? 0 : tryParseInt(split[2], 0);
        NBTTagCompound tags = null;
        
        return PlaceholderConverter.convertItem(item, split[0], count, meta, "", tags);
    }
    
    private static BigItemStack convertItemType2(NBTTagCompound tag)
    {
        String[] split = tag.getString("id").split(" ");
        if(split.length <= 0) return new BigItemStack(ItemStack.EMPTY);
        
        Item item = Item.REGISTRY.getObject(new ResourceLocation(split[0]));
        int count = split.length < 2 ? 1 : tryParseInt(split[1], 1);
        int meta = split.length < 3 ? 0 : tryParseInt(split[2], 0);
        NBTTagCompound tags = !tag.hasKey("tag", 10) ? null : tag.getCompoundTag("tag");
        
        return PlaceholderConverter.convertItem(item, split[0], count, meta, "", tags);
    }
    
    private static int tryParseInt(String text, int def)
    {
        try
        {
            return Integer.parseInt(text);
        } catch(NumberFormatException e)
        {
            return def;
        }
    }
}
