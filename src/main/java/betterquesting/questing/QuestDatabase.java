package betterquesting.questing;

import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.api2.storage.BigDatabase;
import betterquesting.api2.storage.DBEntry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.network.PacketTypeNative;

public final class QuestDatabase extends BigDatabase<IQuest> implements IQuestDatabase
{
	public static final QuestDatabase INSTANCE = new QuestDatabase();
	
	@Override
	public IQuest createNew(int id)
	{
		IQuest q = new QuestInstance();
		q.setParentDatabase(this);
		this.add(id, q);
		return q;
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("config", writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
		base.setTag("progress", writeToNBT(new NBTTagList(), EnumSaveType.PROGRESS));
		tags.setTag("data", base);
		return new QuestingPacket(PacketTypeNative.QUEST_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		NBTTagCompound base = payload.getCompoundTag("data");
		
		readFromNBT(base.getTagList("config", 10), EnumSaveType.CONFIG);
		readFromNBT(base.getTagList("progress", 10), EnumSaveType.PROGRESS);
	}
	
	@Override
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
	{
		switch(saveType)
		{
			case CONFIG:
				return writeToJson_Config(json);
			case PROGRESS:
				return writeToJson_Progress(json, null);
			default:
				break;
		}
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagList json, EnumSaveType saveType)
	{
		switch(saveType)
		{
			case CONFIG:
				readFromJson_Config(json);
				break;
			case PROGRESS:
				readFromJson_Progress(json);
				break;
			default:
				break;
		}
	}
	
	private NBTTagList writeToJson_Config(NBTTagList json)
	{
		for(DBEntry<IQuest> entry : this.getEntries())
		{
			NBTTagCompound jq = new NBTTagCompound();
			entry.getValue().writeToNBT(jq, EnumSaveType.CONFIG);
			jq.setInteger("questID", entry.getID());
			json.appendTag(jq);
		}
		
		return json;
	}
	
	private void readFromJson_Config(NBTTagList nbt)
	{
		this.reset();
		
		for(int i = 0; i < nbt.tagCount(); i++)
		{
			NBTBase entry = nbt.get(i);
			
			if(entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound qTag = (NBTTagCompound)entry;
			
			int qID = qTag.hasKey("questID", 99) ? qTag.getInteger("questID") : -1;
			
			if(qID < 0)
			{
				continue;
			}
			
			IQuest quest = getValue(qID);
			quest = quest != null? quest : this.createNew(qID);
			quest.readFromNBT(qTag, EnumSaveType.CONFIG);
		}
	}
	
	private NBTTagList writeToJson_Progress(NBTTagList json, List<UUID> playerFilter)
	{
		for(DBEntry<IQuest> entry : this.getEntries())
		{
			NBTTagCompound jq = new NBTTagCompound();
			entry.getValue().writeToJson_Progress(jq, playerFilter);
			jq.setInteger("questID", entry.getID());
			json.appendTag(jq);
		}
		
		return json;
	}
	
	private void readFromJson_Progress(NBTTagList json)
	{
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound qTag = (NBTTagCompound)entry;
			
			int qID = qTag.hasKey("questID", 99) ? qTag.getInteger("questID") : -1;
			
			if(qID < 0)
			{
				continue;
			}
			
			IQuest quest = getValue(qID);
			
			if(quest != null)
			{
				quest.readFromNBT(qTag, EnumSaveType.PROGRESS);
			}
		}
	}
	
	public QuestingPacket getSyncPrivatePacket(UUID forPlayer)
	{
		IPartyDatabase partys = QuestingAPI.getAPI(ApiReference.PARTY_DB);
		IParty userParty = partys.getUserParty(forPlayer);
		List<UUID> users = userParty != null ? userParty.getMembers() : Collections.singletonList(forPlayer);
		
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("config", writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
		base.setTag("progress", writeToJson_Progress(new NBTTagList(), users));
		tags.setTag("data", base);
		return new QuestingPacket(PacketTypeNative.QUEST_DATABASE.GetLocation(), tags);
	}
}
