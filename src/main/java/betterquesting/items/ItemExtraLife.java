package betterquesting.items;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.core.BetterQuesting;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.QuestSettings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class ItemExtraLife extends Item
{
	private IIcon iconQuarter;
	private IIcon iconHalf;
	
	public ItemExtraLife()
	{
		this.setTextureName("betterquesting:heart");
		this.setUnlocalizedName("betterquesting.extra_life");
		this.setCreativeTab(BetterQuesting.tabQuesting);
		this.setHasSubtypes(true);
	}

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        return true;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
    	if(stack.getItemDamage() != 0)
    	{
    		return stack;
    	} else if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE))
    	{
    		if(!player.capabilities.isCreativeMode)
    		{
    			stack.stackSize--;
    		}
    		
    		UUID uuid = QuestingAPI.getQuestingUUID(player);
            int lives = LifeDatabase.INSTANCE.getLives(uuid);
    		
    		if(lives >= QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_MAX))
    		{
    			if(!world.isRemote)
    			{
    	    		player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED.toString()).appendSibling(new ChatComponentTranslation("betterquesting.gui.full_lives")));
    			}
	    		
	    		return stack;
    		}

            world.playSoundAtEntity(player, "random.levelup", 1F, 1F);
    		
    		if(!world.isRemote)
    		{
    			LifeDatabase.INSTANCE.setLives(uuid, lives + 1);
    			
    			player.addChatComponentMessage(new ChatComponentTranslation("betterquesting.gui.remaining_lives", EnumChatFormatting.YELLOW.toString() + (lives + 1)));
    		}
    	} else if(!world.isRemote)
    	{
    		player.addChatComponentMessage(new ChatComponentTranslation("betterquesting.msg.heart_disabled"));
    	}
    	
		return stack;
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    @Nonnull
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        switch(stack.getItemDamage()%3)
        {
        	case 2:
        		return this.getUnlocalizedName() + ".quarter";
        	case 1:
        		return this.getUnlocalizedName() + ".half";
        	default:
        		return this.getUnlocalizedName() + ".full";
        }
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack)
    {
		return stack.getItemDamage() == 0;
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @Override
	@SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubItems(Item item, CreativeTabs tab, List list)
    {
    	list.add(new ItemStack(item, 1, 0));
    	list.add(new ItemStack(item, 1, 1));
    	list.add(new ItemStack(item, 1, 2));
    }

    /**
     * Gets an icon index based on an item's damage value
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int dmg)
    {
    	switch(dmg%3)
    	{
    		case 2:
    			return iconQuarter;
    		case 1:
    			return iconHalf;
    		default:
    			return itemIcon;
    	}
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register)
    {
    	iconQuarter = register.registerIcon(this.getIconString() + "_quarter");
    	iconHalf = register.registerIcon(this.getIconString() + "_half");
    	itemIcon = register.registerIcon(this.getIconString() + "_full");
    }
}
