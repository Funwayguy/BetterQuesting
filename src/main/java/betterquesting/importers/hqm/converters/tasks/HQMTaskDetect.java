package betterquesting.importers.hqm.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.importers.hqm.HQMUtilities;
import betterquesting.questing.tasks.TaskFluid;
import betterquesting.questing.tasks.TaskRetrieval;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class HQMTaskDetect
{
	private final boolean consume;
	
	public HQMTaskDetect(boolean consume)
	{
		this.consume = consume;
	}
	
	public ITask[] convertTask(JsonObject json)
	{
		List<ITask> tList = new ArrayList<>();
		TaskRetrieval retTask = new TaskRetrieval();
		TaskFluid fluTask = new TaskFluid();
		
		retTask.consume = this.consume;
		
		for(JsonElement je : JsonHelper.GetArray(json, "items"))
		{
			if(!(je instanceof JsonObject)) continue;
			JsonObject ji = je.getAsJsonObject();
			
			if(ji.has("fluid"))
			{
				fluTask.requiredFluids.add(HQMUtilities.HQMStackT3(ji));
			} else
			{
				retTask.requiredItems.add(HQMUtilities.HQMStackT2(ji));
			}
		}
		
		if(retTask.requiredItems.size() > 0) tList.add(retTask);
		
		if(fluTask.requiredFluids.size() > 0) tList.add(fluTask);
		
		return tList.toArray(new ITask[0]);
	}
}
