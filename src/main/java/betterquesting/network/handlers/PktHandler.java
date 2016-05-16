package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Made to cleanup the old singular packet handler
public abstract class PktHandler
{
	public abstract void handleServer(EntityPlayerMP sender, NBTTagCompound data);
	
	@SideOnly(Side.CLIENT)
	public abstract void handleClient(NBTTagCompound data);
}
