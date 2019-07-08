package betterquesting.api.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Deprecated // Replaced by method references
public interface IPacketHandler
{
	ResourceLocation getRegistryName();
	
	void handleServer(NBTTagCompound tag, EntityPlayerMP sender);
	
	@SideOnly(Side.CLIENT)
	void handleClient(NBTTagCompound tag);
}
