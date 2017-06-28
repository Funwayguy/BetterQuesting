package adv_director.items;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import adv_director.api.api.QuestingAPI;
import adv_director.api.properties.NativeProps;
import adv_director.api.questing.party.IParty;
import adv_director.core.AdvDirector;
import adv_director.questing.party.PartyManager;
import adv_director.storage.LifeDatabase;
import adv_director.storage.QuestSettings;

public class ItemExtraLife extends Item
{
	public ItemExtraLife()
	{
		this.setUnlocalizedName("betterquesting.extra_life");
		this.setCreativeTab(AdvDirector.tabQuesting);
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
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
    	if(stack.getItemDamage() != 0 || hand != EnumHand.MAIN_HAND)
    	{
    		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    	} else if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE))
    	{
    		if(!player.capabilities.isCreativeMode)
    		{
    			stack.stackSize--;
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
    	    		player.addChatComponentMessage(new TextComponentString(TextFormatting.RED.toString()).appendSibling(new TextComponentTranslation("betterquesting.gui.full_lives")));
    			}
	    		
	    		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    		}

            player.worldObj.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1F, 1F);
    		
    		if(!world.isRemote)
    		{
    			if(party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES))
    			{
    				LifeDatabase.INSTANCE.setLives(QuestingAPI.getQuestingUUID(player), lives + 1);
    			} else
    			{
    				LifeDatabase.INSTANCE.setLives(party, lives + 1);
    			}
    			
    			player.addChatComponentMessage(new TextComponentTranslation("betterquesting.gui.remaining_lives", TextFormatting.YELLOW.toString() + (lives + 1)));
    		}
    	} else if(!world.isRemote)
    	{
    		player.addChatComponentMessage(new TextComponentTranslation("betterquesting.msg.heart_disabled"));
    	}
    	
		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
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
    @SuppressWarnings({"unchecked", "rawtypes"})
	@SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list)
    {
    	list.add(new ItemStack(item, 1, 0));
    	list.add(new ItemStack(item, 1, 1));
    	list.add(new ItemStack(item, 1, 2));
    }
}
