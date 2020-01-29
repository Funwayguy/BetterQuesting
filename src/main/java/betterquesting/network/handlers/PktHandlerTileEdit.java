package betterquesting.network.handlers;

import betterquesting.api.network.IPacketHandler;
import betterquesting.blocks.TileSubmitStation;
import betterquesting.network.PacketTypeNative;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class PktHandlerTileEdit implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.EDIT_STATION.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		NBTTagCompound tileData = data.getCompoundTag("tile");
		int x = tileData.getInteger("x");
		int y = tileData.getInteger("y");
		int z = tileData.getInteger("z");

		if (sender.openContainer instanceof ContainerSubmitStation)
		{
			ContainerSubmitStation container = (ContainerSubmitStation) sender.openContainer;
			TileSubmitStation tile = container.getTile();
			if (tile != null && tile.xCoord == x && tile.yCoord == y && tile.zCoord == z)
				tile.SyncTile(tileData);
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
