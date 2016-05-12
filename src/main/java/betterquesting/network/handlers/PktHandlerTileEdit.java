package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import betterquesting.blocks.TileSubmitStation;

public class PktHandlerTileEdit extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
		NBTTagCompound tileData = data.getCompoundTag("tile");
		TileEntity tile = sender.worldObj.getTileEntity(new BlockPos(tileData.getInteger("x"), tileData.getInteger("y"), tileData.getInteger("z")));
		
		if(tile != null && tile instanceof TileSubmitStation)
		{
			((TileSubmitStation)tile).SyncTile(tileData);
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
