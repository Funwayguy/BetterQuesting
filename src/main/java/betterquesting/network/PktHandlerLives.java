package betterquesting.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.lives.BQ_LifeTracker;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
		BQ_LifeTracker tracker = BQ_LifeTracker.get(Minecraft.getMinecraft().thePlayer);
		
		if(tracker != null)
		{
			tracker.loadNBTData(data.getCompoundTag("data"));
		}
		
		return null;
	}
	
}
