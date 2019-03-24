package betterquesting.storage;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.storage.ILifeDatabase;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

// TODO: Make this thread safe
public final class LifeDatabase implements ILifeDatabase
{
	public static final LifeDatabase INSTANCE = new LifeDatabase();
	
	private final HashMap<UUID,Integer> playerLives = new HashMap<>();
	private final HashMap<Integer,Integer> partyLives = new HashMap<>();
	
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
			int def = QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_DEF);
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
		
		playerLives.put(uuid, MathHelper.clamp_int(value, 0, QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_MAX)));
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
			int def = QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_DEF);
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
		
		partyLives.put(id, MathHelper.clamp_int(value, 0, QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_MAX)));
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("lives", writeProgressToNBT(new NBTTagCompound(), null));
		tags.setTag("data", base);
		return new QuestingPacket(PacketTypeNative.LIFE_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		NBTTagCompound base = payload.getCompoundTag("data");
		
		readProgressFromNBT(base.getCompoundTag("lives"), false);
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound json, List<UUID> users)
	{
		NBTTagList jul = new NBTTagList();
		for(Entry<UUID,Integer> entry : playerLives.entrySet())
		{
			NBTTagCompound j = new NBTTagCompound();
			j.setString("uuid", entry.getKey().toString());
			j.setInteger("lives", entry.getValue());
			jul.appendTag(j);
		}
		json.setTag("playerLives", jul);
		
		NBTTagList jpl = new NBTTagList();
		for(Entry<Integer,Integer> entry : partyLives.entrySet())
		{
			NBTTagCompound j = new NBTTagCompound();
			j.setInteger("partyID", entry.getKey());
			j.setInteger("lives", entry.getValue());
			jpl.appendTag(j);
		}
		json.setTag("partyLives", jpl);
		
		return json;
	}
	
	@Override
	public void readProgressFromNBT(NBTTagCompound json, boolean merge)
	{
		playerLives.clear();
		NBTTagList tagList = json.getTagList("playerLives", 10);
		for(int i = 0; i < tagList.tagCount(); i++)
		{
			NBTTagCompound j = tagList.getCompoundTagAt(i);
			
			try
			{
				UUID uuid = UUID.fromString(j.getString("uuid"));
				int lives = j.getInteger("lives");
				playerLives.put(uuid, lives);
			} catch(Exception ignored)
			{
            }
		}
		
		partyLives.clear();
		tagList = json.getTagList("partyLives", 10);
		for(int i = 0; i < tagList.tagCount(); i++)
		{
			NBTTagCompound j = tagList.getCompoundTagAt(i);
			
			int partyID = j.hasKey("partyID", 99) ? j.getInteger("partyID") : -1;
			int lives = j.getInteger("lives");
			
			if(partyID >= 0)
			{
				partyLives.put(partyID, lives);
			}
		}
	}
	
	@Override
	public void reset()
	{
		playerLives.clear();
		partyLives.clear();
	}
}
