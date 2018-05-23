package betterquesting.questing.party;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.NameCache;

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
		tags.setTag("data", writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
		return new QuestingPacket(PacketTypeNative.PARTY_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getTagList("data", 10), EnumSaveType.CONFIG);
	}
	
	@Override
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(DBEntry<IParty> entry : getEntries())
		{
			NBTTagCompound jp = entry.getValue().writeToNBT(new NBTTagCompound(), saveType);
			jp.setInteger("partyID", entry.getID());
			json.appendTag(jp);
		}
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
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
			party.readFromNBT(jp, EnumSaveType.CONFIG);
			
			if(party.getMembers().size() > 0)
			{
				add(partyID, party);
			}
		}
	}
}
