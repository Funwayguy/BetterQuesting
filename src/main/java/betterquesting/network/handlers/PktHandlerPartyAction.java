package betterquesting.network.handlers;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.party.IParty;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyInstance;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;

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
		
		UUID senderID = QuestingAPI.getQuestingUUID(sender);
		
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
				tarParty = PartyManager.INSTANCE.getUserParty(senderID);
			}
			
			if(tarParty != null)
			{
				status = tarParty.getStatus(senderID);
			}
		}
		
		try
		{
			tarUser = UUID.fromString(data.getString("target"));
		} catch(Exception e)
		{
			// In case an unrecognized name was used instead of their UUID
			tarUser = NameCache.INSTANCE.getUUID(data.getString("target"));
		}
		
		if(action == EnumPacketAction.ADD && tarParty == null) // Create new party if not currently in a party
		{
			String name = data.getString("name");
			name = name.length() > 0? name : "New Party";
			IParty nParty = new PartyInstance();
			nParty.setName(name);
			nParty.inviteUser(senderID);
			PartyManager.INSTANCE.add(nParty, PartyManager.INSTANCE.nextKey());
			PacketSender.INSTANCE.sendToAll(PartyManager.INSTANCE.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.REMOVE && tarParty != null && status == EnumPartyStatus.OWNER) // Operator force deletes party or owner disbands it
		{
			PartyManager.INSTANCE.removeKey(partyID);
			PacketSender.INSTANCE.sendToAll(PartyManager.INSTANCE.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.KICK && tarUser != null && tarParty != null && status != null && (status.ordinal() >= 2 || tarUser == senderID)) // Kick/leave party
		{
			tarParty.kickUser(tarUser);
			PacketSender.INSTANCE.sendToAll(tarParty.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.EDIT && tarParty != null && status == EnumPartyStatus.OWNER) // Edit party
		{
			tarParty.readPacket(data);
			PacketSender.INSTANCE.sendToAll(tarParty.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.JOIN && tarParty != null && (isOp || status == EnumPartyStatus.INVITE)) // Join party
		{
			if(isOp)
			{
				tarParty.inviteUser(senderID);
			}
			
			tarParty.setStatus(senderID, EnumPartyStatus.MEMBER);
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
