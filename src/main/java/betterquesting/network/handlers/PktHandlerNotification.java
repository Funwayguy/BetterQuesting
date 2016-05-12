package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.client.QuestNotification;

public class PktHandlerNotification extends PktHandler
{
	
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		ItemStack stack = ItemStack.loadItemStackFromNBT(data.getCompoundTag("Icon"));
		String mainTxt = data.getString("Main");
		String subTxt = data.getString("Sub");
		String sound = data.getString("Sound");
		QuestNotification.ScheduleNotice(mainTxt, subTxt, stack, sound);
	}
	
}
