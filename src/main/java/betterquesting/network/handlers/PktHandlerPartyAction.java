package betterquesting.network.handlers;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.party.IParty;
import betterquesting.network.PacketSender;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;

public class PktHandlerPartyAction implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.PARTY_EDIT.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) // Party management
	{
		if(sender == null)
		{
			return;
		}
		
		/*
		 * If the user is OP than they will have owner permissions of any party
		 * Non OPs can only edit the party they own.
		 */
		boolean isOp = MinecraftServer.getServer().getConfigurationManager().func_152596_g(sender.getGameProfile());
		
		int aID = !data.hasKey("action")? -1 : data.getInteger("action");
		
		if(aID < 0 || aID >= EnumPacketAction.values().length)
		{
			return;
		}
		
		EnumPacketAction action = EnumPacketAction.values()[aID];
		
		int partyID = !data.hasKey("partyID")? -1 : data.getInteger("partyID");
		
		UUID tarUser = null;
		IParty tarParty = null;
		EnumPartyStatus status = null;
		
		if(isOp)
		{
			tarParty = PartyManager.INSTANCE.getValue(partyID);
			status = EnumPartyStatus.OWNER;
		} else
		{
			if(action == EnumPacketAction.JOIN)
			{
				tarParty = PartyManager.INSTANCE.getValue(partyID);
			} else
			{
				tarParty = PartyManager.INSTANCE.getUserParty(sender.getUniqueID());
			}
			
			if(tarParty != null)
			{
				status = tarParty.getStatus(sender.getUniqueID());
			}
		}
		
		try
		{
			tarUser = UUID.fromString(data.getString("target"));
		} catch(Exception e)
		{
			tarUser = null;
		}
		
		if(action == EnumPacketAction.ADD && tarParty == null) // Create new party if not currently in a party
		{
			String name = data.getString("name");
			name = name.length() > 0? name : "New Party";
			IParty nParty = new PartyInstance();
			nParty.setName(name);
			nParty.inviteUser(sender.getUniqueID());
			PartyManager.INSTANCE.add(nParty, PartyManager.INSTANCE.nextID());
			PacketSender.INSTANCE.sendToAll(PartyManager.INSTANCE.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.REMOVE && tarParty != null && status == EnumPartyStatus.OWNER) // Operator force deletes party or owner disbands it
		{
			PartyManager.INSTANCE.remove(partyID);
			PacketSender.INSTANCE.sendToAll(PartyManager.INSTANCE.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.KICK && tarUser != null && tarParty != null && status != null && (status.ordinal() >= 2 || tarUser == sender.getUniqueID())) // Kick/leave party
		{
			tarParty.kickUser(tarUser);
			PacketSender.INSTANCE.sendToAll(tarParty.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.EDIT && tarParty != null && status == EnumPartyStatus.OWNER) // Edit party
		{
			tarParty.readPacket(data);
			PacketSender.INSTANCE.sendToAll(tarParty.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.JOIN && tarParty != null && status != null) // Join party
		{
			tarParty.setStatus(sender.getUniqueID(), EnumPartyStatus.MEMBER);
			PacketSender.INSTANCE.sendToAll(tarParty.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.INVITE && tarParty != null && tarUser != null && status.ordinal() >= 2) // Invite to party
		{
			tarParty.inviteUser(tarUser);
			PacketSender.INSTANCE.sendToAll(tarParty.getSyncPacket());
			return;
		}
		
		return;
	}
	
	@Override
	public void handleClient(NBTTagCompound data) // Nothing technical should be happening client side
	{
	}
}
