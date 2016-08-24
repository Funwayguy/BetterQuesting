package betterquesting.lives;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import betterquesting.api.database.ILifeDatabase;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.party.IParty;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.party.PartyManager;

public final class LifeDatabase implements ILifeDatabase
{
	public static final LifeDatabase INSTANCE = new LifeDatabase();
	
	private int defLives = 3;
	private int maxLives = 10;
	
	private final HashMap<UUID,Integer> playerLives = new HashMap<UUID,Integer>();
	private final HashMap<Integer,Integer> partyLives = new HashMap<Integer,Integer>();
	
	private LifeDatabase()
	{
	}
	
	@Override
	public int getDefaultLives()
	{
		return defLives;
	}
	
	@Override
	public int getMaxLives()
	{
		return maxLives;
	}
	
	public void setDefaultLives(int value)
	{
		this.defLives = value;
	}
	
	public void setMaxLives(int value)
	{
		this.maxLives = value;
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
			playerLives.put(uuid, defLives);
			return defLives;
		}
	}
	
	@Override
	public void setLives(UUID uuid, int value)
	{
		if(uuid == null)
		{
			return;
		}
		
		playerLives.put(uuid, MathHelper.clamp_int(value, 0, maxLives));
	}
	
	@Override
	public int getLives(IParty party)
	{
		int id = party == null? -1 : PartyManager.INSTANCE.getKey(party);
		
		if(id < 0)
		{
			return 0;
		}
		
		if(partyLives.containsKey(id))
		{
			return partyLives.get(id);
		} else
		{
			partyLives.put(id, defLives);
			return defLives;
		}
	}
	
	@Override
	public void setLives(IParty party, int value)
	{
		int id = party == null? -1 : PartyManager.INSTANCE.getKey(party);
		
		if(id < 0)
		{
			return;
		}
		
		partyLives.put(id, MathHelper.clamp_int(value, 0, maxLives));
	}
	
	@Override
	public PreparedPayload getSyncPacket()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("config", writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		base.add("lives", writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		return new PreparedPayload(PacketTypeNative.LIFE_DATABASE.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		JsonObject base = NBTConverter.NBTtoJSON_Compound(payload.getCompoundTag("data"), new JsonObject());
		
		readFromJson(JsonHelper.GetObject(base, "config"), EnumSaveType.CONFIG);
		readFromJson(JsonHelper.GetObject(base, "lives"), EnumSaveType.PROGRESS);
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		switch(saveType)
		{
			case CONFIG:
				return writeToJson_Config(json);
			case PROGRESS:
				return writeToJson_Progress(json);
			default:
				break;
			
		}
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
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
	
	private JsonObject writeToJson_Config(JsonObject json)
	{
		json.addProperty("defLives", defLives);
		json.addProperty("maxLives", maxLives);
		return json;
	}
	
	private void readFromJson_Config(JsonObject json)
	{
		defLives = JsonHelper.GetNumber(json, "defLives", 3).intValue();
		maxLives = JsonHelper.GetNumber(json, "maxLives", 10).intValue();
	}
	
	private JsonObject writeToJson_Progress(JsonObject json)
	{
		JsonArray jul = new JsonArray();
		for(Entry<UUID,Integer> entry : playerLives.entrySet())
		{
			JsonObject j = new JsonObject();
			j.addProperty("uuid", entry.getKey().toString());
			j.addProperty("lives", entry.getValue());
			jul.add(j);
		}
		json.add("playerLives", jul);
		
		JsonArray jpl = new JsonArray();
		for(Entry<Integer,Integer> entry : partyLives.entrySet())
		{
			JsonObject j = new JsonObject();
			j.addProperty("partyID", entry.getKey());
			j.addProperty("lives", entry.getValue());
			jpl.add(j);
		}
		json.add("partyLives", jpl);
		
		return json;
	}
	
	private void readFromJson_Progress(JsonObject json)
	{
		playerLives.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "playerLives"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject j = entry.getAsJsonObject();
			
			try
			{
				UUID uuid = UUID.fromString(JsonHelper.GetString(j, "uuid", ""));
				int lives = JsonHelper.GetNumber(j, "lives", 3).intValue();
				playerLives.put(uuid, lives);
			} catch(Exception e)
			{
				continue;
			}
		}
		
		partyLives.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "partyLives"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject j = entry.getAsJsonObject();
			
			int partyID = JsonHelper.GetNumber(j, "partyID", -1).intValue();
			int lives = JsonHelper.GetNumber(j, "lives", 3).intValue();
			
			if(partyID >= 0)
			{
				partyLives.put(partyID, lives);
			}
		}
	}
}
