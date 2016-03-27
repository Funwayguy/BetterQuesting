package betterquesting.items;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.core.BetterQuesting;
import betterquesting.lives.LifeManager;
import betterquesting.quests.QuestDatabase;

public class ItemExtraLife extends Item
{
	public ItemExtraLife()
	{
		this.setUnlocalizedName("betterquesting.extra_life");
		this.setCreativeTab(BetterQuesting.tabQuesting);
		this.setHasSubtypes(true);
	}

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
	@Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        return EnumActionResult.PASS;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
	@Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
    	if(stack.getItemDamage() != 0 || hand != EnumHand.MAIN_HAND)
    	{
    		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    	} else if(QuestDatabase.bqHardcore)
    	{
    		if(!player.capabilities.isCreativeMode)
    		{
    			stack.stackSize--;
    		}
    		
    		if(LifeManager.getLives(player) >= LifeManager.maxLives)
    		{
	    		player.addChatComponentMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocalFormatted("betterquesting.gui.full_lives")));
	    		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    		}

            player.worldObj.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.entity_player_levelup, SoundCategory.PLAYERS, 1F, 1F);
            
            if(!world.isRemote)
            {
	    		LifeManager.AddRemoveLives(player, 1);
	    		player.addChatComponentMessage(new TextComponentString(I18n.translateToLocalFormatted("betterquesting.gui.remaining_lives", TextFormatting.YELLOW + "" + LifeManager.getLives(player))));
            }
    	} else if(!world.isRemote)
    	{
    		player.addChatComponentMessage(new TextComponentString(I18n.translateToLocalFormatted("betterquesting.msg.heart_disabled")));
    	}
    	
		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }
    
    @Override
    public boolean hasEffect(ItemStack stack)
    {
    	return stack.getItemDamage() == 0;
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
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

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list)
    {
    	list.add(new ItemStack(item, 1, 0));
    	list.add(new ItemStack(item, 1, 1));
    	list.add(new ItemStack(item, 1, 2));
    }
}
