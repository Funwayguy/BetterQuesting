package betterquesting.network;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api2.utils.Tuple2;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.EventHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PacketQuesting implements IMessage
{
	protected NBTTagCompound tags = new NBTTagCompound();
	
	@SuppressWarnings("unused")
	public PacketQuesting() // For use only by forge
	{
	}
	
    public PacketQuesting(NBTTagCompound tags) // Use PacketDataTypes to instantiate new packets
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
			if(packet == null || packet.tags == null || ctx.getServerHandler().playerEntity.mcServer == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BetterQuesting packet server side", new NullPointerException());
				return null;
			}
			
			final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
			final NBTTagCompound message = PacketAssembly.INSTANCE.assemblePacket(sender == null? null : QuestingAPI.getQuestingUUID(sender),packet.tags);
			
			if(message == null)
			{
				return null;
			} else if(!message.hasKey("ID"))
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side without an ID");
				return null;
			}
			
            final Consumer<Tuple2<NBTTagCompound, EntityPlayerMP>> method = PacketTypeRegistry.INSTANCE.getServerHandler(new ResourceLocation(message.getString("ID")));
			
			if(method == null)
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side with an invalid ID: " + message.getString("ID"));
				return null;
			} else if(sender != null)
			{
				EventHandler.scheduleServerTask(Executors.callable(() -> method.accept(new Tuple2<>(message, sender))));
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
			
			final NBTTagCompound message = PacketAssembly.INSTANCE.assemblePacket(null, packet.tags);
			
			if(message == null)
			{
				return null;
			} else if(!message.hasKey("ID"))
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side without an ID");
				return null;
			}
			
			final Consumer<NBTTagCompound> method = PacketTypeRegistry.INSTANCE.getClientHandler(new ResourceLocation(message.getString("ID")));
			
			if(method == null)
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side with an invalid ID: " + message.getString("ID"));
				return null;
			} else
			{
				Minecraft.getMinecraft().func_152343_a(Executors.callable(() -> method.accept(message)));
			}
			
			return null;
		}
	}
}
