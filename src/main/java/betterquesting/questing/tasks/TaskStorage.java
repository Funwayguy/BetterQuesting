package betterquesting.questing.tasks;

import betterquesting.api.placeholders.tasks.FactoryTaskPlaceholder;
import betterquesting.api.placeholders.tasks.TaskPlaceholder;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.api2.storage.SimpleDatabase;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskStorage extends SimpleDatabase<ITask> implements IDatabaseNBT<ITask, ListNBT, ListNBT>
{
	@Override
	public ListNBT writeToNBT(ListNBT json, @Nullable List<Integer> subset)
	{
		for(DBEntry<ITask> entry : getEntries())
		{
			ResourceLocation taskID = entry.getValue().getFactoryID();
			
			CompoundNBT qJson = entry.getValue().writeToNBT(new CompoundNBT());
			qJson.putString("taskID", taskID.toString());
			qJson.putInt("index", entry.getID());
			json.add(qJson);
		}
		return json;
	}
	
	@Override
	public void readFromNBT(ListNBT json, boolean merge)
	{
	    reset();
		List<ITask> unassigned = new ArrayList<>();
		
		for(int i = 0; i < json.size(); i++)
		{
			CompoundNBT jsonTask = json.getCompound(i);
			ResourceLocation loc = new ResourceLocation(jsonTask.getString("taskID"));
			int index = jsonTask.contains("index", 99) ? jsonTask.getInt("index") : -1;
			ITask task = TaskRegistry.INSTANCE.createNew(loc);
			
			if(task instanceof TaskPlaceholder)
			{
				CompoundNBT jt2 = jsonTask.getCompound("orig_data");
				ResourceLocation loc2 = new ResourceLocation(jt2.getString("taskID"));
				ITask t2 = TaskRegistry.INSTANCE.createNew(loc2);
				
				if(t2 != null) // Restored original task
				{
					jsonTask = jt2;
					task = t2;
				}
			}
			
			if(task != null)
			{
				task.readFromNBT(jsonTask);
			} else
			{
				task = new TaskPlaceholder();
                ((TaskPlaceholder)task).setTaskConfigData(jsonTask);
			}
				
            if(index >= 0)
            {
                add(index, task);
            } else
            {
                unassigned.add(task);
            }
		}
		
		for(ITask t : unassigned) add(nextID(), t);
	}
	
	@Override
	public ListNBT writeProgressToNBT(ListNBT json, @Nullable List<UUID> user)
	{
		for(DBEntry<ITask> entry : getEntries())
		{
			ResourceLocation taskID = entry.getValue().getFactoryID();
			
			CompoundNBT qJson = entry.getValue().writeProgressToNBT(new CompoundNBT(), user);
			qJson.putString("taskID", taskID.toString());
			qJson.putInt("index", entry.getID());
			json.add(qJson);
		}
		return json;
	}
	
	@Override
	public void readProgressFromNBT(ListNBT json, boolean merge)
	{
		for(int i = 0; i < json.size(); i++)
		{
			CompoundNBT jsonTask = json.getCompound(i);
			int index = jsonTask.contains("index", 99) ? jsonTask.getInt("index") : -1;
			ResourceLocation loc = new ResourceLocation(jsonTask.getString("taskID"));
			ITask task = getValue(index);
			
			if(task instanceof TaskPlaceholder)
			{
				if(!task.getFactoryID().equals(loc))
				{
					((TaskPlaceholder)task).setTaskProgressData(jsonTask);
				} else
				{
					task.readProgressFromNBT(jsonTask, merge);
				}
			} else if(task != null)
			{
				if(task.getFactoryID().equals(loc))
				{
					task.readProgressFromNBT(jsonTask, merge);
				} else if(FactoryTaskPlaceholder.INSTANCE.getRegistryName().equals(loc)) // Restored placeholder progress
				{
					task.readProgressFromNBT(jsonTask.getCompound("orig_prog"), merge);
				}
			}
		}
	}
}
