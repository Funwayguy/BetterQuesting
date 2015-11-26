package betterquesting.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;
import betterquesting.client.QuestNotification;
import betterquesting.core.BetterQuesting;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
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
				BetterQuesting.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BetterQuesting pack server side");
				return null;
			}
			
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
			
			if(ID == 0) // Quest database synchronization request (not normally required)
			{
				BetterQuesting.logger.log(Level.INFO, "Sending quest database...");
				PacketQuesting packet = new PacketQuesting();
				packet.tags.setInteger("ID", 0);
				JsonObject json = new JsonObject();
				QuestDatabase.writeToJson(json);
				packet.tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
				return packet;
			} else if(ID == 1) // Singular quest synchronization request (not normally required)
			{
				QuestInstance quest = QuestDatabase.getQuestByID(message.tags.getInteger("questID"));
				
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
				return packet;
			} else if(ID == 3 && player != null) // Manual quest detect
			{
				QuestInstance quest = QuestDatabase.getQuestByID(message.tags.getInteger("questID"));
				
				if(quest != null)
				{
					quest.Detect(player);
				}
			} else if(ID == 4 && player != null) // Reward claim attempt
			{
				QuestInstance quest = QuestDatabase.getQuestByID(message.tags.getInteger("questID"));
				NBTTagList choiceData = message.tags.getTagList("ChoiceData", 10);
				
				if(quest != null && quest.CanClaim(player, choiceData))
				{
					quest.Claim(player, choiceData);
				}
			} else if(ID == 5 && player != null) // Edit quest entry
			{
				if(!MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile()))
				{
					BetterQuesting.logger.log(Level.WARN, "Player " + player.getCommandSenderName() + " (UUID:" + player.getUniqueID() + ") tried to edit quests without OP permissions!");
					player.addChatComponentMessage(new ChatComponentText(ChatFormatting.RED + "You need to be OP to edit quests!"));
					return null; // Player is not operator. Do nothing
				}
				
				int action = !message.tags.hasKey("action")? -1 : message.tags.getInteger("action");
				int qID = !message.tags.hasKey("questID")? -1 : message.tags.getInteger("questID");
				QuestInstance quest = QuestDatabase.getQuestByID(qID);
				
				if(action < 0)
				{
					BetterQuesting.logger.log(Level.ERROR, player.getCommandSenderName() + " tried to perform invalid quest edit action: " + action);
					return null;
				} else if(quest == null || qID < 0)
				{
					BetterQuesting.logger.log(Level.ERROR, player.getCommandSenderName() + " tried to edit non-existent quest with ID:" + qID);
					return null;
				}
				
				if(action == 0) // Update quest data
				{
					BetterQuesting.logger.log(Level.INFO, "Player " + player.getCommandSenderName() + " edited quest " + quest.name);
					JsonObject json = NBTConverter.NBTtoJSON_Compound(message.tags.getCompoundTag("Data"), new JsonObject());
					quest.readFromJSON(json);
					quest.UpdateClients();
				} else if(action == 1) // Delete quest
				{
					QuestDatabase.DeleteQuest(quest.questID);
					QuestDatabase.UpdateClients();
				}
			} else if(ID == 6 && player != null)
			{
				if(!MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile()))
				{
					BetterQuesting.logger.log(Level.WARN, "Player " + player.getCommandSenderName() + " (UUID:" + player.getUniqueID() + ") tried to edit quest lines without OP permissions!");
					player.addChatComponentMessage(new ChatComponentText(ChatFormatting.RED + "You need to be OP to edit quests!"));
					return null; // Player is not operator. Do nothing
				}
				
				int action = !message.tags.hasKey("action")? -1 : message.tags.getInteger("action");
				
				if(action < 0)
				{
					BetterQuesting.logger.log(Level.ERROR, player.getCommandSenderName() + " tried to perform invalid quest edit action: " + action);
					return null;
				}
				
				if(action == 0) // Add new QuestLine
				{
					QuestDatabase.questLines.add(new QuestLine());
				} else if(action == 1) // Add new QuestInstance
				{
					new QuestInstance(QuestDatabase.getUniqueID(), true);
				} else if(action == 2) // Edit quest lines
				{
					QuestDatabase.readFromJson_Lines(NBTConverter.NBTtoJSON_Compound(message.tags.getCompoundTag("Data"), new JsonObject()));
				}
				
				QuestDatabase.UpdateClients(); // Update all clients with new quest data
			} else if(ID == 7 && player != null) // Edit parties
			{
				int action = !message.tags.hasKey("action")? -1 : message.tags.getInteger("action");
				String name = message.tags.getString("Party");
				
				if(action == 0) // Create New Party (name is ignored)
				{
					if(PartyManager.GetPartyByName(name) != null) // This should probably be handled client side before it gets to this point
					{
						int i = 0;
						while(PartyManager.GetPartyByName(name + " (" + i + ")") != null)
						{
							i++;
						}
						name = name + " (" + i + ")";
					}
					
					PartyManager.CreateParty(player, name);
					PartyManager.UpdateClients();
				} else if(action == 1) // Kick/leave party
				{
					PartyInstance party = PartyManager.GetPartyByName(name);
					PartyMember member = party == null? null : party.GetMemberData(player.getUniqueID());
					
					if(member == null)
					{
						BetterQuesting.logger.log(Level.ERROR, "Unabled to find party or membership data for " + player.getUniqueID().toString() + " in party " + name, new Exception());
						return null;
					}
					
					UUID uuid;
					
					try
					{
						uuid = UUID.fromString(message.tags.getString("Member"));
						if(uuid == null)
						{
							throw new NullPointerException();
						}
					} catch(Exception e)
					{
						BetterQuesting.logger.log(Level.ERROR, "Unabled to remove user from pary", e);
						return null;
					}
					
					if(!uuid.equals(player.getUniqueID()) && member.GetPrivilege() != 2)
					{
						BetterQuesting.logger.log(Level.ERROR, "Insufficient permission to kick user");
						return null;
					} 
					
					party.LeaveParty(uuid);
					PartyManager.UpdateClients();
				} else if(action == 2) // Edit party
				{
					PartyInstance party = PartyManager.GetPartyByName(name);
					PartyMember member = party == null? null : party.GetMemberData(player.getUniqueID());
					
					if(member == null)
					{
						BetterQuesting.logger.log(Level.ERROR, "Unabled to find party or membership data for " + player.getUniqueID().toString() + " in party " + name, new Exception());
						return null;
					} else if(member.GetPrivilege() != 2)
					{
						BetterQuesting.logger.log(Level.ERROR, "Insufficient permission to edit party");
						return null;
					}
					
					party.readFromJson(NBTConverter.NBTtoJSON_Compound(message.tags.getCompoundTag("Data"), new JsonObject()));
					PartyManager.ApplyNameChange(party);
					PartyManager.UpdateClients();
				} else if(action == 3) // Join party
				{
					PartyInstance party = PartyManager.GetPartyByName(name);
					
					if(party != null)
					{
						if(!party.JoinParty(player.getUniqueID()))
						{
							BetterQuesting.logger.log(Level.ERROR, "Player " + player.getCommandSenderName() + " was unable to join party " + name);
						}
					} else
					{
						BetterQuesting.logger.log(Level.ERROR, "Player " + player.getCommandSenderName() + " was unable to join party " + name);
					}
				}
			}
			
			return null;
		}
	}
	
	public static class HandleClient implements IMessageHandler<PacketQuesting, IMessage>
	{
		@Override
		public IMessage onMessage(PacketQuesting message, MessageContext ctx)
		{
			if(message == null || message.tags == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BetterQuesting pack client side");
				return null;
			}
			
			int ID = !message.tags.hasKey("ID")? -1 : message.tags.getInteger("ID");
			
			if(ID < 0)
			{
				BetterQuesting.logger.log(Level.WARN, "Recieved a packet clientside with an invalid ID");
				return null;
			}
			
			
			if(ID == 0) // Quest database synchronization
			{
				JsonObject json = NBTConverter.NBTtoJSON_Compound(message.tags.getCompoundTag("Database"), new JsonObject());
				QuestDatabase.readFromJson(json);
			} else if(ID == 1) // Singular quest synchronization
			{
				int questID = message.tags.getInteger("questID");
				QuestInstance quest = QuestDatabase.getQuestByID(questID);
				quest = quest != null? quest : new QuestInstance(questID, false); // Server says this exists so create it
				
				JsonObject json = NBTConverter.NBTtoJSON_Compound(message.tags.getCompoundTag("Data"), new JsonObject());
				quest.readFromJSON(json);
				
				for(QuestLine line : QuestDatabase.questLines)
				{
					line.BuildTree(); // If a prerequisite change was made, trees need updating
				}
				
				QuestDatabase.updateUI = true; // Tell all UIs they need updating
			} else if(ID == 2) // Party database synchronization
			{
				JsonObject json = NBTConverter.NBTtoJSON_Compound(message.tags.getCompoundTag("Parties"), new JsonObject());
				PartyManager.readFromJson(json);
			} else if(ID == 3) // Screen notification
			{
				ItemStack stack = ItemStack.loadItemStackFromNBT(message.tags.getCompoundTag("Icon"));
				String mainTxt = message.tags.getString("Main");
				String subTxt = message.tags.getString("Sub");
				int sound = message.tags.getInteger("Sound");
				QuestNotification.ScheduleNotice(mainTxt, subTxt, stack, sound);
			}
			
			return null;
		}
	}
}
