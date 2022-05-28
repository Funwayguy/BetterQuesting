package betterquesting.blocks;

import betterquesting.core.BetterQuesting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockObservationStation extends BlockContainer {

    private IIcon topIcon;

    public BlockObservationStation() {
        super(Material.wood);
        this.setHardness(1);
        this.setBlockName("betterquesting.observation_station");
        this.setBlockTextureName("betterquesting:observation_station");
        this.setCreativeTab(BetterQuesting.tabQuesting);
    }

    @Override
    public TileEntity createNewTileEntity(World w, int meta) {
        return new TileObservationStation();
    }

    @Override
    public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase living, ItemStack is) {
        super.onBlockPlacedBy(w, x, y, z, living, is);
        if (w.isRemote) return;
        if (!(living instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) living;
        TileEntity tile = w.getTileEntity(x, y, z);
        if (!(tile instanceof TileObservationStation)) return;
        TileObservationStation os = (TileObservationStation) tile;
        os.owner = player.getGameProfile().getId();
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return (side == ForgeDirection.UP.ordinal() || side == ForgeDirection.DOWN.ordinal()) ? topIcon : blockIcon;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        this.blockIcon = iconRegister.registerIcon(this.getTextureName() + "_side");
        this.topIcon = iconRegister.registerIcon(this.getTextureName() + "_top");
    }
}
