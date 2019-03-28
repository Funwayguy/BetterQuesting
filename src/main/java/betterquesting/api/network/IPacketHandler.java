package betterquesting.api.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public interface IPacketHandler
{
	ResourceLocation getRegistryName();
	
	void handleServer(NBTTagCompound tag, EntityPlayerMP sender);
	
	@SideOnly(Side.CLIENT)
	void handleClient(NBTTagCompound tag);
}
