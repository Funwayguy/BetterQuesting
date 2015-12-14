package bq_standard.network;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.utils.BigItemStack;
import bq_standard.client.gui.GuiLootChest;
import bq_standard.core.BQ_Standard;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketStandard implements IMessage
{
	NBTTagCompound tags = new NBTTagCompound();
	
	public PacketStandard()
	{
	}
	
	public PacketStandard(NBTTagCompound payload)
	{
		tags = payload;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		tags = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		if(BQ_Standard.proxy.isClient() && Minecraft.getMinecraft().thePlayer != null)
		{
			tags.setString("Sender", Minecraft.getMinecraft().thePlayer.getUniqueID().toString());
			tags.setInteger("Dimension", Minecraft.getMinecraft().thePlayer.dimension);
		}
		
		ByteBufUtils.writeTag(buf, tags);
	}
	
	public static class HandlerServer implements IMessageHandler<PacketStandard,IMessage>
	{
		@Override
		public IMessage onMessage(PacketStandard message, MessageContext ctx)
		{
			return null;
		}
	}
	
	public static class HandlerClient implements IMessageHandler<PacketStandard,IMessage>
	{
		@Override
		public IMessage onMessage(PacketStandard message, MessageContext ctx)
		{
			if(message == null || message.tags == null)
			{
				return null;
			}
			
			int ID = !message.tags.hasKey("ID")? -1 : message.tags.getInteger("ID");
			
			if(ID == 0)
			{
				String title = message.tags.getString("title");
				ArrayList<BigItemStack> rewards = new ArrayList<BigItemStack>();
				
				NBTTagList list = message.tags.getTagList("rewards", 10);
				
				for(int i = 0; i < list.tagCount(); i++)
				{
					BigItemStack stack = BigItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
					
					if(stack != null)
					{
						rewards.add(stack);
					}
				}
				
				Minecraft.getMinecraft().displayGuiScreen(new GuiLootChest(rewards, title));
			}
			
			return null;
		}
	}
}
