package betterquesting.api.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.tasks.ITask;

/**
 * Holds a cache of active quests and tasks per player. Cache will automatically be updated on a regular basis.
 * Using this instead of iterating over the whole database should be much faster and reduce TPS load.
 */
public class QuestCache
{
	public static final QuestCache INSTANCE = new QuestCache();
	
	// Player UUID > Quest IDs > Task IDs
	private final HashMap<UUID,HashMap<Integer,List<Integer>>> rawCache = new HashMap<UUID,HashMap<Integer,List<Integer>>>();
	
	private QuestCache()
	{
	}
	
	public void updateCache(EntityPlayer player)
	{
		if(player == null)
		{
			return;
		}
		
		UUID uuid = QuestingAPI.getQuestingUUID(player);
		
		HashMap<Integer,List<Integer>> pCache = new HashMap<Integer,List<Integer>>();
		rawCache.put(uuid, pCache);
		
		IQuestDatabase questDB = QuestingAPI.getAPI(ApiReference.QUEST_DB);
		List<Integer> idList = questDB.getAllKeys();
		
		for(int qID : idList)
		{
			IQuest quest = questDB.getValue(qID);
			
			if(quest == null || !quest.isUnlocked(uuid) || !quest.canSubmit(player))
			{
				continue;
			}
			
			List<Integer> tList = new ArrayList<Integer>();
			
			for(int tID : quest.getTasks().getAllKeys())
			{
				ITask task = quest.getTasks().getValue(tID);
				
				if(task != null && !task.isComplete(uuid))
				{
					tList.add(tID);
				}
			}
			
			pCache.put(qID, tList);
		}
	}
	
	/**
	 * Returns a cached list of active quests
	 */
	public List<IQuest> getActiveQuests(UUID uuid)
	{
		List<IQuest> list = new ArrayList<IQuest>();
		HashMap<Integer,List<Integer>> pCache = rawCache.get(uuid);
		pCache = pCache != null? pCache : new HashMap<Integer,List<Integer>>();
		
		for(int id : pCache.keySet())
		{
			IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(id);
			
			if(quest != null)
			{
				list.add(quest);
			}
		}
		
		return list;
	}
	
	/**
	 * Returns a cached list of all active tasks with references to their parent quest
	 */
	public Map<ITask,IQuest> getActiveTasks(UUID uuid)
	{
		return getActiveTasks(uuid, ITask.class);
	}
	
	/**
	 * Returns a cached list of active tasks of the given type with references to their parent quest
	 */
	@SuppressWarnings("unchecked")
	public <T extends ITask> Map<T,IQuest> getActiveTasks(UUID uuid, Class<T> type)
	{
		Map<T,IQuest> list = new HashMap<T,IQuest>();
		HashMap<Integer,List<Integer>> pCache = rawCache.get(uuid);
		pCache = pCache != null? pCache : new HashMap<Integer,List<Integer>>();
		
		for(Entry<Integer,List<Integer>> entry : pCache.entrySet())
		{
			IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(entry.getKey());
			
			if(quest == null)
			{
				continue;
			}
			
			for(int tID : entry.getValue())
			{
				ITask task = quest.getTasks().getValue(tID);
				
				if(task != null && type.isAssignableFrom(task.getClass()))
				{
					list.put((T)task, quest);
				}
			}
		}
		
		return list;
	}
	
	public void reset()
	{
		rawCache.clear();
	}
}
