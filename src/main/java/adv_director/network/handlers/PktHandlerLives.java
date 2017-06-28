package adv_director.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import adv_director.api.events.DatabaseEvent;
import adv_director.api.network.IPacketHandler;
import adv_director.network.PacketTypeNative;
import adv_director.storage.LifeDatabase;

public class PktHandlerLives implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.LIFE_DATABASE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleClient(NBTTagCompound data)
	{
		LifeDatabase.INSTANCE.readPacket(data);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
}
