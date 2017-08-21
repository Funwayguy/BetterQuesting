package betterquesting.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.QuestSettings;

public class ItemExtraLife extends Item
{
	public ItemExtraLife()
	{
		this.setUnlocalizedName("betterquesting.extra_life");
		this.setCreativeTab(BetterQuesting.tabQuesting);
		this.setHasSubtypes(true);
	}

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
    	ItemStack stack = player.getHeldItem(hand);
    	
    	if(stack.getItemDamage() != 0 || hand != EnumHand.MAIN_HAND)
    	{
    		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    	} else if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE))
    	{
    		if(!player.capabilities.isCreativeMode)
    		{
    			stack.grow(-1);
    		}
    		
    		int lives = 0;
    		IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(player));
    		
    		if(party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES))
    		{
    			lives = LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(player));
    		} else
    		{
    			lives = LifeDatabase.INSTANCE.getLives(party);
    		}
    		
    		if(lives >= QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_MAX).intValue())
    		{
    			if(!world.isRemote)
    			{
    	    		player.sendStatusMessage(new TextComponentString(TextFormatting.RED.toString()).appendSibling(new TextComponentTranslation("betterquesting.gui.full_lives")), true);
    			}
	    		
	    		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    		}

            player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1F, 1F);
    		
    		if(!world.isRemote)
    		{
    			if(party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES))
    			{
    				LifeDatabase.INSTANCE.setLives(QuestingAPI.getQuestingUUID(player), lives + 1);
    			} else
    			{
    				LifeDatabase.INSTANCE.setLives(party, lives + 1);
    			}
    			
    			player.sendStatusMessage(new TextComponentTranslation("betterquesting.gui.remaining_lives", TextFormatting.YELLOW.toString() + (lives + 1)), true);
    		}
    	} else if(!world.isRemote)
    	{
    		player.sendStatusMessage(new TextComponentTranslation("betterquesting.msg.heart_disabled"), true);
    	}
    	
		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
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
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
    {
    	if(this.isInCreativeTab(tab))
    	{
	    	list.add(new ItemStack(this, 1, 0));
	    	list.add(new ItemStack(this, 1, 1));
	    	list.add(new ItemStack(this, 1, 2));
    	}
    }
}
