package betterquesting.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.api.storage.ILifeDatabase;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyManager;

public final class LifeDatabase implements ILifeDatabase
{
	public static final LifeDatabase INSTANCE = new LifeDatabase();
	
	private final HashMap<UUID,Integer> playerLives = new HashMap<>();
	private final HashMap<Integer,Integer> partyLives = new HashMap<>();
	
	private LifeDatabase()
	{
	}
	
	@Override
	public int getLives(UUID uuid)
	{
		if(uuid == null)
		{
			return 0;
		}
		
		if(playerLives.containsKey(uuid))
		{
			return playerLives.get(uuid);
		} else
		{
			int def = QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_DEF).intValue();
			playerLives.put(uuid, def);
			return def;
		}
	}
	
	@Override
	public void setLives(UUID uuid, int value)
	{
		if(uuid == null)
		{
			return;
		}
		
		playerLives.put(uuid, MathHelper.clamp(value, 0, QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_MAX).intValue()));
	}
	
	@Override
	public int getLives(IParty party)
	{
		int id = party == null? -1 : PartyManager.INSTANCE.getID(party);
		
		if(id < 0)
		{
			return 0;
		}
		
		if(partyLives.containsKey(id))
		{
			return partyLives.get(id);
		} else
		{
			int def = QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_DEF).intValue();
			partyLives.put(id, def);
			return def;
		}
	}
	
	@Override
	public void setLives(IParty party, int value)
	{
		int id = party == null? -1 : PartyManager.INSTANCE.getID(party);
		
		if(id < 0)
		{
			return;
		}
		
		partyLives.put(id, MathHelper.clamp(value, 0, QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_MAX).intValue()));
	}
	
	@Override
	public QuestingPacket getProgressSyncPacket(UUID player)
	{
		IPartyDatabase partys = QuestingAPI.getAPI(ApiReference.PARTY_DB);
		IParty userParty = partys.getUserParty(player);
		List<UUID> users = userParty != null ? userParty.getMembers() : Collections.singletonList(player);
		
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("lives", writeToJson_Progress(new NBTTagCompound(), users));
		tags.setTag("data", base);
		tags.setBoolean("isProgressUpdate", true);
		
		return new QuestingPacket(PacketTypeNative.LIFE_DATABASE.GetLocation(), tags);
	}
	
	public QuestingPacket getSyncPrivatePacket(UUID forPlayer)
	{
		IPartyDatabase partys = QuestingAPI.getAPI(ApiReference.PARTY_DB);
		IParty userParty = partys.getUserParty(forPlayer);
		List<UUID> users = userParty != null ? userParty.getMembers() : Collections.singletonList(forPlayer);
		
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("config", writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG));
		base.setTag("lives", writeToJson_Progress(new NBTTagCompound(), users));
		tags.setTag("data", base);
		return new QuestingPacket(PacketTypeNative.LIFE_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("config", writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG));
		base.setTag("lives", writeToNBT(new NBTTagCompound(), EnumSaveType.PROGRESS));
		tags.setTag("data", base);
		return new QuestingPacket(PacketTypeNative.LIFE_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		NBTTagCompound base = payload.getCompoundTag("data");
		boolean isProgressUpdate = payload.getBoolean("isProgressUpdate");
		if(!isProgressUpdate)
		{
			readFromNBT(base.getCompoundTag("config"), EnumSaveType.CONFIG);
		}
		readFromNBT(base.getCompoundTag("lives"), EnumSaveType.PROGRESS);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.PROGRESS)
		{
			return json;
		}
		
		return writeToJson_Progress(json, null);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.PROGRESS)
		{
			return;
		}
		
		readFromJson_Progress(json);
	}
	
	private NBTTagCompound writeToJson_Progress(NBTTagCompound json, List<UUID> userFilter)
	{
		NBTTagList jul = new NBTTagList();
		for(Entry<UUID,Integer> entry : playerLives.entrySet())
		{
			if(userFilter != null && !userFilter.contains(entry.getKey())) continue;
			
			NBTTagCompound j = new NBTTagCompound();
			j.setString("uuid", entry.getKey().toString());
			j.setInteger("lives", entry.getValue());
			jul.appendTag(j);
		}
		json.setTag("playerLives", jul);
		
		NBTTagList jpl = new NBTTagList();
		for(Entry<Integer,Integer> entry : partyLives.entrySet())
		{
			if(userFilter != null)
			{
				IParty p = QuestingAPI.getAPI(ApiReference.PARTY_DB).getValue(entry.getKey());
				if (p == null || !p.getMembers().stream().anyMatch(pm -> userFilter.contains(pm))) continue;
			}
			
			NBTTagCompound j = new NBTTagCompound();
			j.setInteger("partyID", entry.getKey());
			j.setInteger("lives", entry.getValue());
			jpl.appendTag(j);
		}
		json.setTag("partyLives", jpl);
		
		return json;
	}
	
	private void readFromJson_Progress(NBTTagCompound json)
	{
		playerLives.clear();
		NBTTagList tagList = json.getTagList("playerLives", 10);
		for(int i = 0; i < tagList.tagCount(); i++)
		{
			NBTBase entry = tagList.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound j = (NBTTagCompound)entry;
			
			try
			{
				UUID uuid = UUID.fromString(j.getString("uuid"));
				int lives = j.getInteger("lives");
				playerLives.put(uuid, lives);
			} catch(Exception e)
			{
				continue;
			}
		}
		
		partyLives.clear();
		tagList = json.getTagList("partyLives", 10);
		for(int i = 0; i < tagList.tagCount(); i++)
		{
			NBTBase entry = tagList.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound j = (NBTTagCompound)entry;
			
			int partyID = j.hasKey("partyID", 99) ? j.getInteger("partyID") : -1;
			int lives = j.getInteger("lives");
			
			if(partyID >= 0)
			{
				partyLives.put(partyID, lives);
			}
		}
	}

	public void reset()
	{
		playerLives.clear();
		partyLives.clear();
	}
}
