package betterquesting.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.database.IJsonSaveLoad;
import betterquesting.api.database.IRegStorage;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.tasks.ITaskBase;
import betterquesting.api.utils.JsonHelper;
import betterquesting.registry.TaskRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TaskStorage implements IRegStorage<ITaskBase>, IJsonSaveLoad<JsonArray>
{
	private final HashMap<Integer,ITaskBase> database = new HashMap<Integer,ITaskBase>();
	
	@Override
	public int nextID()
	{
		int id = 0;
		
		while(database.containsKey(id))
		{
			id++;
		}
		
		return id;
	}

	@Override
	public boolean add(ITaskBase obj, int id)
	{
		if(obj == null || database.containsKey(id) || database.containsKey(id))
		{
			return false;
		}
		
		database.put(id, obj);
		return true;
	}
	
	@Override
	public boolean remove(int id)
	{
		return database.remove(id) != null;
	}
	
	@Override
	public ITaskBase getValue(int id)
	{
		return database.get(id);
	}

	@Override
	public int getKey(ITaskBase obj)
	{
		int id = -1;
		
		for(Entry<Integer,ITaskBase> entry : database.entrySet())
		{
			if(entry.getValue() == obj)
			{
				return entry.getKey();
			}
		}
		
		return id;
	}

	@Override
	public List<ITaskBase> getAllValues()
	{
		return new ArrayList<ITaskBase>(database.values());
	}

	@Override
	public List<Integer> getAllKeys()
	{
		return new ArrayList<Integer>(database.keySet());
	}
	
	@Override
	public int size()
	{
		return database.size();
	}

	@Override
	public JsonArray writeToJson(JsonArray json, EnumSaveType saveType)
	{
		switch(saveType)
		{
			case CONFIG:
				writeToJson_Config(json);
				break;
			case PROGRESS:
				writeToJson_Progress(json);
				break;
			default:
				break;
		}
		
		return json;
	}

	@Override
	public void readFromJson(JsonArray json, EnumSaveType saveType)
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
	
	private JsonArray writeToJson_Config(JsonArray json)
	{
		for(Entry<Integer,ITaskBase> entry : database.entrySet())
		{
			ResourceLocation taskID = entry.getValue().getFactoryID();
			
			JsonObject qJson = entry.getValue().writeToJson(new JsonObject(), EnumSaveType.CONFIG);
			qJson.addProperty("taskID", taskID.toString());
			qJson.addProperty("index", entry.getKey());
			json.add(qJson);
		}
		return json;
	}
	
	private void readFromJson_Config(JsonArray json)
	{
		ArrayList<ITaskBase> unassigned = new ArrayList<ITaskBase>();
		
		for(JsonElement entry : json)
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jsonTask = entry.getAsJsonObject();
			ResourceLocation loc = new ResourceLocation(JsonHelper.GetString(jsonTask, "taskID", ""));
			int index = JsonHelper.GetNumber(jsonTask, "index", -1).intValue();
			ITaskBase task = TaskRegistry.INSTANCE.createTask(loc);
			
			if(task != null)
			{
				task.readFromJson(jsonTask, EnumSaveType.CONFIG);
				
				if(index >= 0)
				{
					add(task, index);
				} else
				{
					unassigned.add(task);
				}
			}
		}
		
		for(ITaskBase t : unassigned)
		{
			add(t, nextID());
		}
	}
	
	private JsonArray writeToJson_Progress(JsonArray json)
	{
		for(Entry<Integer,ITaskBase> entry : database.entrySet())
		{
			ResourceLocation taskID = entry.getValue().getFactoryID();
			
			JsonObject qJson = entry.getValue().writeToJson(new JsonObject(), EnumSaveType.PROGRESS);
			qJson.addProperty("taskID", taskID.toString());
			qJson.addProperty("index", entry.getKey());
			json.add(qJson);
		}
		return json;
	}
	
	private void readFromJson_Progress(JsonArray json)
	{
		for(int i = 0; i < json.size(); i++)
		{
			JsonElement entry = json.get(i);
			
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jsonTask = entry.getAsJsonObject();
			int index = JsonHelper.GetNumber(jsonTask, "index", -1).intValue();
			ITaskBase task = getValue(index);
			
			if(task != null)
			{
				task.readFromJson(jsonTask, EnumSaveType.PROGRESS);
			}
		}
	}
}
