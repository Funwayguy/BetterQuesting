package betterquesting.storage;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.storage.ILifeDatabase;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public final class LifeDatabase implements ILifeDatabase
{
	public static final LifeDatabase INSTANCE = new LifeDatabase();
	
	private final HashMap<UUID,Integer> playerLives = new HashMap<>();
	
	@Override
	public synchronized int getLives(@Nonnull UUID uuid)
	{
		return playerLives.computeIfAbsent(uuid, (k) -> QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_DEF));
	}
	
	@Override
	public synchronized void setLives(@Nonnull UUID uuid, int value)
	{
		playerLives.put(uuid, MathHelper.clamp(value, 0, QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_MAX)));
	}
	
	@Override
	public synchronized CompoundNBT writeToNBT(CompoundNBT nbt, @Nullable List<UUID> users)
	{
		ListNBT jul = new ListNBT();
		
        for(Entry<UUID,Integer> entry : playerLives.entrySet())
        {
            if(users != null && !users.contains(entry.getKey())) continue;
            CompoundNBT j = new CompoundNBT();
            j.putString("uuid", entry.getKey().toString());
            j.putInt("lives", entry.getValue());
            jul.add(j);
        }
		nbt.put("playerLives", jul);
		
		return nbt;
	}
	
	@Override
	public synchronized void readFromNBT(CompoundNBT nbt, boolean merge)
	{
		if(!merge) playerLives.clear();
		ListNBT tagList = nbt.getList("playerLives", 10);
		for(int i = 0; i < tagList.size(); i++)
		{
			CompoundNBT j = tagList.getCompound(i);
			
			try
			{
				UUID uuid = UUID.fromString(j.getString("uuid"));
				int lives = j.getInt("lives");
				playerLives.put(uuid, lives);
			} catch(Exception ignored){}
		}
	}
	
	@Override
	public synchronized void reset()
	{
		playerLives.clear();
	}
}
