package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.blocks.TileSubmitStation;

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
		TileEntity tile = sender.worldObj.getTileEntity(tileData.getInteger("x"), tileData.getInteger("y"), tileData.getInteger("z"));
		
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
