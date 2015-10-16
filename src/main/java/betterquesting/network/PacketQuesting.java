package betterquesting.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketQuesting implements IMessage
{
	NBTTagCompound tags = new NBTTagCompound();
	
	public PacketQuesting()
	{
	}
	
	public PacketQuesting(NBTTagCompound tags)
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
		if(BetterQuesting.proxy.isClient() && Minecraft.getMinecraft().thePlayer != null)
		{
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			tags.setString("Sender", player.getUniqueID().toString());
			tags.setInteger("Dimension", player.dimension);
		}
		
		ByteBufUtils.writeTag(buf, tags);
	}
	
	public static class HandleServer implements IMessageHandler<PacketQuesting, IMessage>
	{
		@Override
		public IMessage onMessage(PacketQuesting message, MessageContext ctx)
		{
			int ID = !message.tags.hasKey("ID")? -1 : message.tags.getInteger("ID");
			
			if(ID < 0)
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
			
			if(ID == 0) // Quest database synchronization request
			{
				BetterQuesting.logger.log(Level.INFO, "Sending quest database...");
				PacketQuesting packet = new PacketQuesting();
				packet.tags.setInteger("ID", 0);
				JsonObject json = new JsonObject();
				QuestDatabase.writeToJSON(json);
				packet.tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
				return packet;
			} else if(ID == 1) // Singular quest synchronization request
			{
				QuestInstance quest = QuestDatabase.getQuest(message.tags.getInteger("questID"));
				
				if(quest != null)
				{
					JsonObject json = new JsonObject();
					quest.writeToJSON(json);
					PacketQuesting packet = new PacketQuesting();
					packet.tags.setInteger("ID", 1);
					packet.tags.setInteger("questID", quest.questID);
					packet.tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
					return packet;
				}
			} else if(ID == 2) // Party synchronization request
			{
				JsonObject json = new JsonObject();
				PartyManager.writeToJson(json);
				PacketQuesting packet = new PacketQuesting();
				packet.tags.setInteger("ID", 2);
				packet.tags.setTag("Parties", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
			} else if(ID == 3 && player != null) // Manual quest detect
			{
				QuestInstance quest = QuestDatabase.getQuest(message.tags.getInteger("questID"));
				
				if(quest != null)
				{
					quest.Detect(player);
				}
			} else if(ID == 4 && player != null) // Reward claim attempt
			{
				QuestInstance quest = QuestDatabase.getQuest(message.tags.getInteger("questID"));
				
				if(quest != null && quest.CanClaim(player))
				{
					quest.Claim(player);
				}
			} else if(ID == 5) // Reserved for something I can't remember
			{
				
			}
			
			return null;
		}
	}
	
	public static class HandleClient implements IMessageHandler<PacketQuesting, IMessage>
	{
		@Override
		public IMessage onMessage(PacketQuesting message, MessageContext ctx)
		{
			int ID = !message.tags.hasKey("ID")? -1 : message.tags.getInteger("ID");
			
			if(ID < 0)
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet clientside with an invalid ID");
				return null;
			}
			
			
			if(ID == 0) // Quest database synchronization
			{
				BetterQuesting.logger.log(Level.INFO, "Updating local quest database...");
				JsonObject json = NBTConverter.NBTtoJSON_Compound(message.tags.getCompoundTag("Database"), new JsonObject());
				QuestDatabase.readFromJSON(json);
			} else if(ID == 1) // Singular quest synchronization
			{
				int questID = message.tags.getInteger("questID");
				QuestInstance quest = QuestDatabase.getQuest(questID);
				quest = quest != null? quest : new QuestInstance(questID, false); // Server says this exists so create it
				
				JsonObject json = NBTConverter.NBTtoJSON_Compound(message.tags.getCompoundTag("Data"), new JsonObject());
				quest.readFromJSON(json);
			} else if(ID == 2) // Party database synchronization
			{
				JsonObject json = NBTConverter.NBTtoJSON_Compound(message.tags.getCompoundTag("Parties"), new JsonObject());
				PartyManager.readFromJson(json);
			}
			
			return null;
		}
	}
}
