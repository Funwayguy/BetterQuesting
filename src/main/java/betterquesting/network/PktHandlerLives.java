package betterquesting.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.lives.IHardcoreLives;
import betterquesting.lives.LifeManager;

public class PktHandlerLives extends PktHandler
{
	@Override
	public IMessage handleServer(EntityPlayer sender, NBTTagCompound data)
	{
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage handleClient(NBTTagCompound data)
	{
		IHardcoreLives tracker = Minecraft.getMinecraft().thePlayer.getCapability(LifeManager.LIFE_CAP, null);
		
		if(tracker != null)
		{
			tracker.readFromNBT(data.getCompoundTag("data"));
		}
		
		return null;
	}
	
}
