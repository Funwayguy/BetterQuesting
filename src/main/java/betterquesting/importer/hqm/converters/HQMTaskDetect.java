package betterquesting.importer.hqm.converters;

import java.util.ArrayList;
import betterquesting.importer.hqm.HQMImporter;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.JsonHelper;
import bq_standard.tasks.TaskFluid;
import bq_standard.tasks.TaskRetrieval;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HQMTaskDetect extends HQMTask
{
	boolean consume = false;
	
	public HQMTaskDetect(boolean consume)
	{
		this.consume = consume;
	}
	
	@Override
	public ArrayList<TaskBase> Convert(JsonObject json)
	{
		ArrayList<TaskBase> tList = new ArrayList<TaskBase>();
		TaskRetrieval retTask = new TaskRetrieval();
		TaskFluid fluTask = new TaskFluid();
		
		retTask.consume = this.consume;
		
		for(JsonElement je : JsonHelper.GetArray(json, "items"))
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			JsonObject ji = je.getAsJsonObject();
			
			if(ji.has("fluid"))
			{
				fluTask.requiredFluids.add(HQMImporter.HQMStackT3(ji));
			} else
			{
				retTask.requiredItems.add(HQMImporter.HQMStackT2(ji));
			}
		}
		
		if(retTask.requiredItems.size() > 0)
		{
			tList.add(retTask);
		}
		
		if(fluTask.requiredFluids.size() > 0)
		{
			tList.add(fluTask);
		}
		
		return tList;
	}
}
