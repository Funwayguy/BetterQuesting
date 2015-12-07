package betterquesting.importer.hqm.converters;

import java.util.ArrayList;
import betterquesting.importer.hqm.HQMImporter;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.JsonHelper;
import bq_standard.tasks.TaskCrafting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HQMTaskCraft extends HQMTask
{
	@Override
	public ArrayList<TaskBase> Convert(JsonObject json)
	{
		ArrayList<TaskBase> tList = new ArrayList<TaskBase>();
		
		TaskCrafting task = new TaskCrafting();
		
		for(JsonElement element : JsonHelper.GetArray(json, "items"))
		{
			if(element == null || !element.isJsonObject())
			{
				continue;
			}
			
			task.requiredItems.add(HQMImporter.HQMStackT2(element.getAsJsonObject()));
		}
		
		tList.add(task);
		
		return tList;
	}
}
