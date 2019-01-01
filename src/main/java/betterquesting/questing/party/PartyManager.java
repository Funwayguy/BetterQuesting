package betterquesting.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.NameCache;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyManager extends SimpleDatabase<IParty> implements IPartyDatabase
{
	public static final PartyManager INSTANCE = new PartyManager();
	
	@Override
	public IParty getUserParty(UUID uuid)
	{
		for(DBEntry<IParty> entry : getEntries())
		{
			EnumPartyStatus status = entry.getValue().getStatus(uuid);
			
			if(status != null && status != EnumPartyStatus.INVITE)
			{
				return entry.getValue();
			}
		}
		
		return null;
	}
	
	@Override
	public List<Integer> getPartyInvites(UUID uuid)
	{
		List<Integer> invites = new ArrayList<>();
		
		boolean isOp = NameCache.INSTANCE.isOP(uuid);
		
		for(DBEntry<IParty> entry : getEntries())
		{
			if(isOp || entry.getValue().getStatus(uuid) == EnumPartyStatus.INVITE)
			{
				invites.add(entry.getID());
			}
		}
		
		return invites;
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", writeProgressToNBT(new NBTTagList(), null));
		return new QuestingPacket(PacketTypeNative.PARTY_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		readProgressFromNBT(payload.getTagList("data", 10), false);
	}
	
	@Override
	public NBTTagList writeProgressToNBT(NBTTagList json, List<UUID> users)
	{
		for(DBEntry<IParty> entry : getEntries())
		{
			NBTTagCompound jp = entry.getValue().writeToNBT(new NBTTagCompound());
			jp.setInteger("partyID", entry.getID());
			json.appendTag(jp);
		}
		
		return json;
	}
	
	@Override
	public void readProgressFromNBT(NBTTagList json, boolean merge)
	{
		reset();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase element = json.get(i);
			
			if(element.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound jp = (NBTTagCompound)element;
			
			int partyID = jp.hasKey("partyID", 99) ? jp.getInteger("partyID") : -1;
			
			if(partyID < 0)
			{
				continue;
			}
			
			IParty party = new PartyInstance();
			party.readFromNBT(jp);
			
			if(party.getMembers().size() > 0)
			{
				add(partyID, party);
			}
		}
	}
}
