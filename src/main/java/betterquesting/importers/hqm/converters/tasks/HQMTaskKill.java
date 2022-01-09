package betterquesting.importers.hqm.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.questing.tasks.TaskHunt;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class HQMTaskKill
{
	public ITask[] convertTask(JsonObject json)
	{
		List<ITask> tList = new ArrayList<>();
		
		for(JsonElement je : JsonHelper.GetArray(json, "mobs"))
		{
			if(!(je instanceof JsonObject)) continue;
			JsonObject jMob = je.getAsJsonObject();
			
			TaskHunt task = new TaskHunt();
			task.idName = JsonHelper.GetString(jMob, "mob", "minecraft:zombie");
			task.required = JsonHelper.GetNumber(jMob, "kills", 1).intValue();
			task.subtypes = !JsonHelper.GetBoolean(jMob, "exact", false);
			tList.add(task);
		}
		
		return tList.toArray(new ITask[0]);
	}
}
