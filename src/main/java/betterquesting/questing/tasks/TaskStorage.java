package betterquesting.questing.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.placeholders.tasks.FactoryTaskPlaceholder;
import betterquesting.api.placeholders.tasks.TaskPlaceholder;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.storage.IRegStorageBase;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TaskStorage implements IRegStorageBase<Integer,ITask>, IJsonSaveLoad<JsonArray>
{
	private final HashMap<Integer,ITask> database = new HashMap<Integer,ITask>();
	
	@Override
	public Integer nextKey()
	{
		int id = 0;
		
		while(database.containsKey(id))
		{
			id++;
		}
		
		return id;
	}

	@Override
	public boolean add(ITask obj, Integer id)
	{
		if(obj == null || database.containsKey(id) || database.containsKey(id))
		{
			return false;
		}
		
		database.put(id, obj);
		return true;
	}
	
	@Override
	public boolean removeKey(Integer id)
	{
		return database.remove(id) != null;
	}
	
	@Override
	public boolean removeValue(ITask task)
	{
		return removeKey(getKey(task));
	}
	
	@Override
	public ITask getValue(Integer id)
	{
		return database.get(id);
	}

	@Override
	public Integer getKey(ITask obj)
	{
		int id = -1;
		
		for(Entry<Integer,ITask> entry : database.entrySet())
		{
			if(entry.getValue() == obj)
			{
				return entry.getKey();
			}
		}
		
		return id;
	}

	@Override
	public List<ITask> getAllValues()
	{
		return new ArrayList<ITask>(database.values());
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
	public void reset()
	{
		database.clear();
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
		for(Entry<Integer,ITask> entry : database.entrySet())
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
		database.clear();
		
		ArrayList<ITask> unassigned = new ArrayList<ITask>();
		
		for(JsonElement entry : json)
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			JsonObject jsonTask = entry.getAsJsonObject();
			ResourceLocation loc = new ResourceLocation(JsonHelper.GetString(jsonTask, "taskID", ""));
			int index = JsonHelper.GetNumber(jsonTask, "index", -1).intValue();
			ITask task = TaskRegistry.INSTANCE.createTask(loc);
			
			if(task instanceof TaskPlaceholder)
			{
				JsonObject jt2 = JsonHelper.GetObject(jsonTask, "orig_data");
				ResourceLocation loc2 = new ResourceLocation(JsonHelper.GetString(jt2, "taskID", ""));
				ITask t2 = TaskRegistry.INSTANCE.createTask(loc2);
				
				if(t2 != null) // Restored original task
				{
					jsonTask = jt2;
					task = t2;
				}
			}
			
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
			} else
			{
				TaskPlaceholder tph = new TaskPlaceholder();
				tph.setTaskData(jsonTask, EnumSaveType.CONFIG);
				
				if(index >= 0)
				{
					add(tph, index);
				} else
				{
					unassigned.add(tph);
				}
			}
		}
		
		for(ITask t : unassigned)
		{
			add(t, nextKey());
		}
	}
	
	private JsonArray writeToJson_Progress(JsonArray json)
	{
		for(Entry<Integer,ITask> entry : database.entrySet())
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
			ResourceLocation loc = new ResourceLocation(JsonHelper.GetString(jsonTask, "taskID", ""));
			ITask task = getValue(index);
			
			if(task instanceof TaskPlaceholder)
			{
				if(!task.getFactoryID().equals(loc))
				{
					((TaskPlaceholder)task).setTaskData(jsonTask, EnumSaveType.PROGRESS);
				} else
				{
					task.readFromJson(jsonTask, EnumSaveType.PROGRESS);
				}
			} else if(task != null)
			{
				if(task.getFactoryID().equals(loc))
				{
					task.readFromJson(jsonTask, EnumSaveType.PROGRESS);
				} else if(FactoryTaskPlaceholder.INSTANCE.getRegistryName().equals(loc)) // Restored placeholder progress
				{
					task.readFromJson(JsonHelper.GetObject(jsonTask, "orig_prog"), EnumSaveType.PROGRESS);
				}
			}
		}
	}
}
