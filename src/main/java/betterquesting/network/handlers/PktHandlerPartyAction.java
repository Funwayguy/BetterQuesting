package betterquesting.network.handlers;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.party.PartyManager;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;

public class PktHandlerPartyAction extends PktHandler
{
	
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data) // Party management
	{
		if(sender == null)
		{
			return;
		}
		
		int action = !data.hasKey("action")? -1 : data.getInteger("action");
		String name = data.getString("Party");
		
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
			
			PartyManager.CreateParty(sender, name);
			PartyManager.UpdateClients();
		} else if(action == 1) // Kick/leave party
		{
			PartyInstance party = PartyManager.GetPartyByName(name);
			PartyMember member = party == null? null : party.GetMemberData(sender.getUniqueID());
			
			if(member == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unabled to find party or membership data for " + sender.getUniqueID().toString() + " in party " + name, new Exception());
				return;
			}
			
			UUID uuid;
			
			try
			{
				uuid = UUID.fromString(data.getString("Member"));
				if(uuid == null)
				{
					throw new NullPointerException();
				}
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unabled to remove user from pary", e);
				return;
			}
			
			if(!uuid.equals(sender.getUniqueID()) && member.GetPrivilege() != 2)
			{
				BetterQuesting.logger.log(Level.ERROR, "Insufficient permission to kick user");
				return;
			} 
			
			party.LeaveParty(uuid);
			PartyManager.UpdateClients();
		} else if(action == 2) // Edit party
		{
			PartyInstance party = PartyManager.GetPartyByName(name);
			PartyMember member = party == null? null : party.GetMemberData(sender.getUniqueID());
			
			if(member == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unabled to find party or membership data for " + sender.getUniqueID().toString() + " in party " + name, new Exception());
				return;
			} else if(member.GetPrivilege() != 2)
			{
				BetterQuesting.logger.log(Level.ERROR, "Insufficient permission to edit party");
				return;
			}
			
			party.readFromJson(NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Data"), new JsonObject()));
			PartyManager.ApplyNameChange(party);
			PartyManager.UpdateClients();
		} else if(action == 3) // Join party
		{
			PartyInstance party = PartyManager.GetPartyByName(name);
			
			if(party != null)
			{
				if(!party.JoinParty(sender.getUniqueID()))
				{
					BetterQuesting.logger.log(Level.ERROR, "Player " + sender.getCommandSenderName() + " was unable to join party " + name);
				} else
				{
					PartyManager.UpdateClients();
				}
			} else
			{
				BetterQuesting.logger.log(Level.ERROR, "Player " + sender.getCommandSenderName() + " was unable to join party " + name);
			}
		} else if(action == 4) // Invite to party
		{
			PartyInstance party = PartyManager.GetPartyByName(name);
			PartyMember member = party == null? null : party.GetMemberData(sender.getUniqueID());
			
			if(member == null)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unabled to find party or membership data for " + sender.getUniqueID().toString() + " in party " + name, new Exception());
				return;
			} else if(member.GetPrivilege() != 2)
			{
				BetterQuesting.logger.log(Level.ERROR, "Insufficient permission to invite to party");
				return;
			}
			
			String username = data.getString("Member");
			EntityPlayer inviteUser = MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
			
			if(inviteUser != null)
			{
				PartyManager.ManualUserCache(inviteUser);
				party.InvitePlayer(inviteUser.getUniqueID());
				PartyManager.UpdateClients();
			}
		}
		
		return;
	}
	
	@Override
	public void handleClient(NBTTagCompound data) // Nothing technical should be happening client side
	{
	}
	
}
