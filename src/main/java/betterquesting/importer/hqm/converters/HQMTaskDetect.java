package betterquesting.importer.hqm.converters;

import java.util.ArrayList;
import betterquesting.importer.hqm.HQMImporter;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.quests.tasks.TaskRetrieval;
import betterquesting.utils.JsonHelper;
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
		TaskRetrieval task = new TaskRetrieval();
		tList.add(task);
		
		task.consume = this.consume;
		
		for(JsonElement je : JsonHelper.GetArray(json, "items"))
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			JsonObject ji = je.getAsJsonObject();
			
			if(ji.has("fluid"))
			{
				task.requiredFluids.add(HQMImporter.HQMStackT3(ji));
			} else
			{
				task.requiredItems.addAll(HQMImporter.HQMStackT2(ji));
			}
		}
		
		return tList;
	}
}
