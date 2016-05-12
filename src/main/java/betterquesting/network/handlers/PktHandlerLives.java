package betterquesting.network.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.lives.IHardcoreLives;
import betterquesting.lives.LifeManager;

public class PktHandlerLives extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
		return;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleClient(NBTTagCompound data)
	{
		IHardcoreLives tracker = Minecraft.getMinecraft().thePlayer.getCapability(LifeManager.LIFE_CAP, null);
		
		if(tracker != null)
		{
			tracker.readFromNBT(data.getCompoundTag("data"));
		}
	}
	
}
