package betterquesting.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import betterquesting.client.QuestNotification;

public class PktHandlerNotification extends PktHandler
{
	
	@Override
	public IMessage handleServer(EntityPlayer sender, NBTTagCompound data)
	{
		return null;
	}
	
	@Override
	public IMessage handleClient(NBTTagCompound data)
	{
		ItemStack stack = ItemStack.loadItemStackFromNBT(data.getCompoundTag("Icon"));
		String mainTxt = data.getString("Main");
		String subTxt = data.getString("Sub");
		int sound = data.getInteger("Sound");
		QuestNotification.ScheduleNotice(mainTxt, subTxt, stack, sound);
		return null;
	}
	
}
