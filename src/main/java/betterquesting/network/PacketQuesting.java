package betterquesting.network;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketQuesting implements IMessage
{
	private NBTTagCompound tags = new NBTTagCompound();
	
	public PacketQuesting() // For use only by forge
	{
	}
	
	private PacketQuesting(NBTTagCompound tags) // Use PacketDataTypes to instantiate new packets
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
		if(BetterQuesting.proxy.isClient() && Minecraft.getMinecraft().thePlayer != null) // Auto set upon delivery to prevent forgery
		{
			tags.setString("Sender", Minecraft.getMinecraft().thePlayer.getUniqueID().toString());
			tags.setInteger("Dimension", Minecraft.getMinecraft().thePlayer.dimension);
		}
		
		ByteBufUtils.writeTag(buf, tags);
	}
	
	public static class HandleServer implements IMessageHandler<PacketQuesting, IMessage>
	{
		@Override
		public IMessage onMessage(PacketQuesting message, MessageContext ctx)
		{
			if(message == null || message.tags == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BetterQuesting pack server side", new NullPointerException());
				return null;
			}
			
			int ID = !message.tags.hasKey("ID")? -1 : message.tags.getInteger("ID");
			
			if(ID < 0 || ID >= PacketDataType.values().length)
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet serverside with an invalid ID");
				return null;
			}
			
			EntityPlayer player = null;
			
			if(message.tags.hasKey("Sender"))
			{
				try
				{
					WorldServer world = MinecraftServer.getServer().worldServerForDimension(message.tags.getInteger("Dimension"));
					player = world.func_152378_a(UUID.fromString(message.tags.getString("Sender")));
				} catch(Exception e)
				{
					
				}
			}
			
			PacketDataType dataType = PacketDataType.values()[ID];
			PktHandler handler = pktHandlers.get(dataType);
			
			if(handler != null)
			{
				return handler.handleServer(player, message.tags);
			} else
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to find valid packet handler for data type: " + dataType.toString());
				return null;
			}
		}
	}
	
	public static class HandleClient implements IMessageHandler<PacketQuesting, IMessage>
	{
		@Override
		public IMessage onMessage(PacketQuesting message, MessageContext ctx)
		{
			if(message == null || message.tags == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BetterQuesting pack client side", new NullPointerException());
				return null;
			}
			
			int ID = !message.tags.hasKey("ID")? -1 : message.tags.getInteger("ID");
			
			if(ID < 0 || ID >= PacketDataType.values().length)
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet clientside with an invalid ID");
				return null;
			}
			
			PacketDataType dataType = PacketDataType.values()[ID];
			PktHandler handler = pktHandlers.get(dataType);
			
			if(handler != null)
			{
				return handler.handleClient(message.tags);
			} else
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to find valid packet handler for data type: " + dataType.toString());
				return null;
			}
		}
	}
	
	static HashMap<PacketDataType, PktHandler> pktHandlers = new HashMap<PacketDataType, PktHandler>();
	
	static
	{
		pktHandlers.put(PacketDataType.QUEST_DATABASE, 	new PktHandlerQuestDB());
		pktHandlers.put(PacketDataType.PARTY_DATABASE, 	new PktHandlerPartyDB());
		pktHandlers.put(PacketDataType.QUEST_SYNC, 		new PktHandlerQuestSync());
		pktHandlers.put(PacketDataType.QUEST_EDIT, 		new PktHandlerQuestEdit());
		pktHandlers.put(PacketDataType.LINE_EDIT, 		new PktHandlerLineEdit());
		pktHandlers.put(PacketDataType.DETECT, 			new PktHandlerDetect());
		pktHandlers.put(PacketDataType.CLAIM, 			new PktHandlerClaim());
		pktHandlers.put(PacketDataType.PARTY_ACTION, 	new PktHandlerPartyAction());
		pktHandlers.put(PacketDataType.LIFE_SYNC, 		new PktHandlerLives());
		pktHandlers.put(PacketDataType.NOTIFICATION, 	new PktHandlerNotification());
		pktHandlers.put(PacketDataType.EDIT_STATION, 	new PktHandlerTileEdit());
	}
	
	public enum PacketDataType
	{
		QUEST_DATABASE,
		PARTY_DATABASE,
		QUEST_SYNC,
		QUEST_EDIT,
		LINE_EDIT,
		DETECT,
		CLAIM,
		PARTY_ACTION,
		LIFE_SYNC,
		EDIT_STATION,
		NOTIFICATION;
		
		public PacketQuesting makePacket(NBTTagCompound payload)
		{
			payload.setInteger("ID", this.ordinal()); // Ensure this is set correctly
			return new PacketQuesting(payload);
		}
	}
}
