package betterquesting.network.handlers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

// Made to cleanup the old singular packet handler
public abstract class PktHandler
{
	public abstract void handleServer(EntityPlayerMP sender, NBTTagCompound data);
	
	@SideOnly(Side.CLIENT)
	public abstract void handleClient(NBTTagCompound data);
}
