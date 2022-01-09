package betterquesting;

import betterquesting.api2.storage.INBTPartial;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

public class ScoreboardBQ implements INBTPartial<NBTTagList, UUID>
{
    public static final ScoreboardBQ INSTANCE = new ScoreboardBQ();
    
	private final TreeMap<String, ScoreBQ> objectives = new TreeMap<>();
	
	public synchronized int getScore(@Nonnull UUID uuid, @Nonnull String scoreName)
	{
		ScoreBQ score = objectives.get(scoreName);
		return score == null ? 0 : score.getScore(uuid);
	}
	
	public synchronized void setScore(@Nonnull UUID uuid, @Nonnull String scoreName, int value)
	{
		ScoreBQ score = objectives.computeIfAbsent(scoreName, (key) -> new ScoreBQ());
		score.setScore(uuid, value);
	}
	
	@Override
	public synchronized void readFromNBT(NBTTagList nbt, boolean merge)
	{
        if(!merge) objectives.clear();
		for(int i = 0; i < nbt.tagCount(); i++)
		{
			NBTTagCompound jObj = nbt.getCompoundTagAt(i);
			ScoreBQ score = objectives.computeIfAbsent(jObj.getString("name"), (key) -> new ScoreBQ());
			score.readFromNBT(jObj.getTagList("scores", 10), merge);
		}
	}
	
	@Override
	public synchronized NBTTagList writeToNBT(NBTTagList nbt, @Nullable List<UUID> users)
	{
		for(Entry<String,ScoreBQ> entry : objectives.entrySet())
		{
			NBTTagCompound jObj = new NBTTagCompound();
			jObj.setString("name", entry.getKey());
			jObj.setTag("scores", entry.getValue().writeToNBT(new NBTTagList(), users));
			nbt.appendTag(jObj);
		}
		
		return nbt;
	}
}
