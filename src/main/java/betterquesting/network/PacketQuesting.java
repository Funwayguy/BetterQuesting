package betterquesting.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.network.IPacketHandler;
import betterquesting.core.BetterQuesting;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketQuesting implements IMessage
{
	protected NBTTagCompound tags = new NBTTagCompound();
	
	public PacketQuesting() // For use only by forge
	{
	}
	
	protected PacketQuesting(NBTTagCompound tags) // Use PacketDataTypes to instantiate new packets
	{
		this.tags = tags;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		tags = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, tags);
	}
	
	public static class HandleServer implements IMessageHandler<PacketQuesting, IMessage>
	{
		@Override
		public IMessage onMessage(PacketQuesting packet, MessageContext ctx)
		{
			if(packet == null || packet.tags == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BetterQuesting packet server side", new NullPointerException());
				return null;
			}
			
			EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
			NBTTagCompound message = PacketAssembly.INSTANCE.assemblePacket(sender == null? null : sender.getGameProfile().getId(),packet.tags);
			
			if(message == null)
			{
				return null;
			} else if(!message.hasKey("ID"))
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side without an ID");
				return null;
			}
			
			IPacketHandler handler = PacketTypeRegistry.INSTANCE.getPacketHandler(new ResourceLocation(message.getString("ID")));
			
			if(handler == null)
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side with an invalid ID: " + message.getString("ID"));
				return null;
			} else
			{
				handler.handleServer(message, sender);
			}
			
			return null;
		}
	}
	
	public static class HandleClient implements IMessageHandler<PacketQuesting, IMessage>
	{
		@Override
		public IMessage onMessage(PacketQuesting packet, MessageContext ctx)
		{
			if(packet == null || packet.tags == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BetterQuesting packet client side", new NullPointerException());
				return null;
			}
			
			NBTTagCompound message = PacketAssembly.INSTANCE.assemblePacket(null, packet.tags);
			
			if(message == null)
			{
				return null;
			} else if(!message.hasKey("ID"))
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side without an ID");
				return null;
			}
			
			IPacketHandler handler = PacketTypeRegistry.INSTANCE.getPacketHandler(new ResourceLocation(message.getString("ID")));
			
			if(handler == null)
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side with an invalid ID: " + message.getString("ID"));
				return null;
			} else
			{
				handler.handleClient(message);
			}
			
			return null;
		}
	}
}
