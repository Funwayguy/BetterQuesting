package betterquesting.blocks;

import betterquesting.core.BetterQuesting;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class BlockSubmitStation extends BlockContainer {
  public BlockSubmitStation() {
    super(Material.WOOD);
    setHardness(1);
    setTranslationKey("betterquesting.submit_station");
    setCreativeTab(BetterQuesting.tabQuesting);
  }

  @Override
  public TileEntity createNewTileEntity(@Nonnull World world, int meta) {
    return new TileSubmitStation();
  }

  /**
   * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
   */
  @Nonnull
  @Override
  public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
    return EnumBlockRenderType.MODEL;
  }

  /**
   * Called upon block activation (right click on the block.)
   */
  @Override
  public boolean onBlockActivated(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state,
                                  @Nonnull EntityPlayer player, @Nonnull EnumHand hand,
                                  @Nonnull EnumFacing heldItem, float side, float hitX, float hitY) {
    if (!world.isRemote) {
      player.openGui(BetterQuesting.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
    }
    return true;
  }

  public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
    TileEntity te = world.getTileEntity(pos);
    if (te != null) {
      IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
      if (handler != null) {
        for (int i = 0; i < handler.getSlots(); i++) {
          ItemStack stack = handler.extractItem(i, Integer.MAX_VALUE, false);
          InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
      }
    }
    super.breakBlock(world, pos, state);
  }
}
