package betterquesting.blocks;

public class BlockSubmitStation //extends BlockContainer
{
	/*public BlockSubmitStation()
	{
		super(Material.WOOD);
		this.setHardness(1);
		this.setTranslationKey("betterquesting.submit_station");
		this.setCreativeTab(BetterQuesting.tabQuesting);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileSubmitStation();
	}
	
 
	@Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
    
	@Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing heldItem, float side, float hitX, float hitY)
    {
    	if(!world.isRemote)
    	{
    		player.openGui(BetterQuesting.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
    	}
        return true;
    }

    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileSubmitStation tileStation = (TileSubmitStation)world.getTileEntity(pos);
        
        if(tileStation != null)
        {
        	InventoryHelper.dropInventoryItems(world, pos, tileStation);
        }
        
        super.breakBlock(world, pos, state);
    }*/
}
