package betterquesting.items;

public class ItemExtraLife// extends Item
{
	/*public ItemExtraLife()
	{
		this.setTranslationKey("betterquesting.extra_life");
		this.setCreativeTab(BetterQuesting.tabQuesting);
		this.setHasSubtypes(true);
	}
	
    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
    {
    	ItemStack stack = player.getHeldItem(hand);
    	
    	if(stack.getItemDamage() != 0 || hand != EnumHand.MAIN_HAND)
    	{
    		return new ActionResult<>(EnumActionResult.PASS, stack);
    	} else if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE))
    	{
    		if(!player.capabilities.isCreativeMode)
    		{
    			stack.grow(-1);
    		}
    		
            int lives = LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(player));
    		
    		if(lives >= QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_MAX))
    		{
    			if(!world.isRemote)
    			{
    	    		player.sendStatusMessage(new TextComponentString(TextFormatting.RED.toString()).appendSibling(new TextComponentTranslation("betterquesting.gui.full_lives")), true);
    			}
	    		
	    		return new ActionResult<>(EnumActionResult.PASS, stack);
    		}

            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1F, 1F);
    		
    		if(!world.isRemote)
    		{
                LifeDatabase.INSTANCE.setLives(QuestingAPI.getQuestingUUID(player), lives + 1);
    			
    			player.sendStatusMessage(new TextComponentTranslation("betterquesting.gui.remaining_lives", TextFormatting.YELLOW.toString() + (lives + 1)), true);
    		}
    	} else if(!world.isRemote)
    	{
    		player.sendStatusMessage(new TextComponentTranslation("betterquesting.msg.heart_disabled"), true);
    	}
    	
		return new ActionResult<>(EnumActionResult.PASS, stack);
    }
    
    @Override
    @Nonnull
    public String getTranslationKey(ItemStack stack)
    {
        switch(stack.getItemDamage()%3)
        {
        	case 2:
        		return this.getTranslationKey() + ".quarter";
        	case 1:
        		return this.getTranslationKey() + ".half";
        	default:
        		return this.getTranslationKey() + ".full";
        }
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack)
    {
		return stack.getItemDamage() == 0;
    }
    
    @Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list)
    {
    	if(this.isInCreativeTab(tab))
    	{
	    	list.add(new ItemStack(this, 1, 0));
	    	list.add(new ItemStack(this, 1, 1));
	    	list.add(new ItemStack(this, 1, 2));
    	}
    }*/
}
