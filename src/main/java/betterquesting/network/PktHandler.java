package betterquesting.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

// Made to cleanup the old singular packet handler
public abstract class PktHandler
{
	public abstract IMessage handleServer(EntityPlayer sender, NBTTagCompound data);
	public abstract IMessage handleClient(NBTTagCompound data);
}
