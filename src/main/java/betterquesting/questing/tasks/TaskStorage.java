package betterquesting.questing.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.placeholders.tasks.FactoryTaskPlaceholder;
import betterquesting.api.placeholders.tasks.TaskPlaceholder;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.storage.IRegStorageBase;

public class TaskStorage implements IRegStorageBase<Integer,ITask>, INBTSaveLoad<NBTTagList>
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
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
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
		for(Entry<Integer,ITask> entry : database.entrySet())
		{
			ResourceLocation taskID = entry.getValue().getFactoryID();
			
			NBTTagCompound qJson = entry.getValue().writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG);
			qJson.setString("taskID", taskID.toString());
			qJson.setInteger("index", entry.getKey());
			json.appendTag(qJson);
		}
		return json;
	}
	
	private void readFromJson_Config(NBTTagList json)
	{
		database.clear();
		
		ArrayList<ITask> unassigned = new ArrayList<ITask>();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound jsonTask = (NBTTagCompound)entry;
			ResourceLocation loc = new ResourceLocation(jsonTask.getString("taskID"));
			int index = jsonTask.hasKey("index", 99) ? jsonTask.getInteger("index") : -1;
			ITask task = TaskRegistry.INSTANCE.createTask(loc);
			
			if(task instanceof TaskPlaceholder)
			{
				NBTTagCompound jt2 = jsonTask.getCompoundTag("orig_data");
				ResourceLocation loc2 = new ResourceLocation(jt2.getString("taskID"));
				ITask t2 = TaskRegistry.INSTANCE.createTask(loc2);
				
				if(t2 != null) // Restored original task
				{
					jsonTask = jt2;
					task = t2;
				}
			}
			
			if(task != null)
			{
				task.readFromNBT(jsonTask, EnumSaveType.CONFIG);
				
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
	
	private NBTTagList writeToJson_Progress(NBTTagList json)
	{
		for(Entry<Integer,ITask> entry : database.entrySet())
		{
			ResourceLocation taskID = entry.getValue().getFactoryID();
			
			NBTTagCompound qJson = entry.getValue().writeToNBT(new NBTTagCompound(), EnumSaveType.PROGRESS);
			qJson.setString("taskID", taskID.toString());
			qJson.setInteger("index", entry.getKey());
			json.appendTag(qJson);
		}
		return json;
	}
	
	private void readFromJson_Progress(NBTTagList json)
	{
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound jsonTask = (NBTTagCompound)entry;
			int index = jsonTask.hasKey("index", 99) ? jsonTask.getInteger("index") : -1;
			ResourceLocation loc = new ResourceLocation(jsonTask.getString("taskID"));
			ITask task = getValue(index);
			
			if(task instanceof TaskPlaceholder)
			{
				if(!task.getFactoryID().equals(loc))
				{
					((TaskPlaceholder)task).setTaskData(jsonTask, EnumSaveType.PROGRESS);
				} else
				{
					task.readFromNBT(jsonTask, EnumSaveType.PROGRESS);
				}
			} else if(task != null)
			{
				if(task.getFactoryID().equals(loc))
				{
					task.readFromNBT(jsonTask, EnumSaveType.PROGRESS);
				} else if(FactoryTaskPlaceholder.INSTANCE.getRegistryName().equals(loc)) // Restored placeholder progress
				{
					task.readFromNBT(jsonTask.getCompoundTag("orig_prog"), EnumSaveType.PROGRESS);
				}
			}
		}
	}
}
