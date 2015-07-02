package betterquesting.quests;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.logging.log4j.Level;
import cpw.mods.fml.common.Loader;
import betterquesting.core.BetterQuesting;
import betterquesting.quests.types.QuestBase;

/**
 * Registry for all known quest types. Questing packs should register their custom types here for proper NBT saving/loading
 */
public class QuestRegistry
{
	static HashMap<String, Class<? extends QuestBase>> questRegistry = new HashMap<String, Class<? extends QuestBase>>();
	
	public static void RegisterQuest(Class<? extends QuestBase> quest, Object mod, String idName)
	{
		try
		{
			if(quest == null)
			{
				throw new NullPointerException("Tried to register null quest");
			} else if(Loader.instance().getReversedModObjectList().containsKey(mod))
			{
				throw new IllegalArgumentException("Tried to register a quest without a vialid mod instance");
			}
			
			try
			{
				quest.getDeclaredConstructor();
			} catch(NoSuchMethodException e)
			{
				throw new NoSuchMethodException("Registered quest is missing a default constructor with 0 arguemnts");
			}
			
			String fullName = Loader.instance().getReversedModObjectList().get(mod).getModId() + ":" + idName;
			
			if(questRegistry.containsKey(fullName))
			{
				throw new IllegalStateException("Cannot register dupliate quest type '" + fullName + "'");
			}
			
			questRegistry.put(fullName, quest);
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while trying to register quest type:", e);
		}
	}
	
	public static String GetID(Class<? extends QuestBase> quest)
	{
		for(String idName : questRegistry.keySet())
		{
			if(questRegistry.get(idName) == quest)
			{
				return idName;
			}
		}
		
		return null;
	}
	
	public static Class<? extends QuestBase> GetQuest(String idName)
	{
		return questRegistry.get(idName);
	}
	
	public static ArrayList<String> GetTypeList()
	{
		return new ArrayList<String>(questRegistry.keySet());
	}
	
	public static QuestBase InstatiateQuest(String idName)
	{
		try
		{
			return questRegistry.get(idName).newInstance();
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to instatiate quest: " + idName, e);
			return null;
		}
	}
}
