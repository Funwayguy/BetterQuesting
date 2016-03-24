package betterquesting.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import betterquesting.blocks.TileSubmitStation;

public class PktHandlerTileEdit extends PktHandler
{
	@Override
	public IMessage handleServer(EntityPlayer sender, NBTTagCompound data)
	{
		NBTTagCompound tileData = data.getCompoundTag("tile");
		TileEntity tile = sender.worldObj.getTileEntity(new BlockPos(tileData.getInteger("x"), tileData.getInteger("y"), tileData.getInteger("z")));
		
		if(tile != null && tile instanceof TileSubmitStation)
		{
			((TileSubmitStation)tile).SyncTile(tileData);
		}
		return null;
	}
	
	@Override
	public IMessage handleClient(NBTTagCompound data)
	{
		return null;
	}
}
