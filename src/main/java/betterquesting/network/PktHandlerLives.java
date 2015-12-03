package betterquesting.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.lives.BQ_LifeTracker;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class PktHandlerLives extends PktHandler
{
	@Override
	public IMessage handleServer(EntityPlayer sender, NBTTagCompound data)
	{
		return null;
	}

	@Override
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
