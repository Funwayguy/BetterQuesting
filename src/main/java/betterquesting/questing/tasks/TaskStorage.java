package betterquesting.questing.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.api2.storage.SimpleDatabase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.placeholders.tasks.FactoryTaskPlaceholder;
import betterquesting.api.placeholders.tasks.TaskPlaceholder;
import betterquesting.api.questing.tasks.ITask;

public class TaskStorage extends SimpleDatabase<ITask> implements IDatabaseNBT<ITask, NBTTagList>
{
	@Override
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
	{
		switch(saveType)
		{
			case CONFIG:
				return writeToJson_Config(json);
			case PROGRESS:
				return writeToJson_Progress(json, null);
			default:
				return json;
		}
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
		for(DBEntry<ITask> entry : getEntries())
		{
			ResourceLocation taskID = entry.getValue().getFactoryID();
			
			NBTTagCompound qJson = entry.getValue().writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG);
			qJson.setString("taskID", taskID.toString());
			qJson.setInteger("index", entry.getID());
			json.appendTag(qJson);
		}
		return json;
	}
	
	private void readFromJson_Config(NBTTagList json)
	{
		reset();
		
		List<ITask> unassigned = new ArrayList<>();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry.getId() != 10)
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
					add(index, task);
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
					add(index, tph);
				} else
				{
					unassigned.add(tph);
				}
			}
		}
		
		for(ITask t : unassigned)
		{
			add(nextID(), t);
		}
	}
	
	public NBTTagList writeToJson_Progress(NBTTagList json, List<UUID> userFilter)
	{
		for(DBEntry<ITask> entry : getEntries())
		{
			ResourceLocation taskID = entry.getValue().getFactoryID();
			
			NBTTagCompound qJson = entry.getValue().writeProgressToJson(new NBTTagCompound(), userFilter);
			qJson.setString("taskID", taskID.toString());
			qJson.setInteger("index", entry.getID());
			json.appendTag(qJson);
		}
		return json;
	}
	
	private void readFromJson_Progress(NBTTagList json)
	{
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry.getId() != 10)
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
