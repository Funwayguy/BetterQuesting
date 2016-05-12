package betterquesting.network.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.lives.BQ_LifeTracker;

public class PktHandlerLives extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleClient(NBTTagCompound data)
	{
		BQ_LifeTracker tracker = BQ_LifeTracker.get(Minecraft.getMinecraft().thePlayer);
		
		if(tracker != null)
		{
			tracker.loadNBTData(data.getCompoundTag("data"));
		}
	}
	
}
