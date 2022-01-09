package betterquesting.importers.hqm.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.questing.tasks.TaskTame;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class HQMTaskTame
{
    public ITask[] convertTask(JsonObject json)
    {
		List<ITask> tList = new ArrayList<>();
		
		for(JsonElement je : JsonHelper.GetArray(json, "tame"))
		{
			if(!(je instanceof JsonObject)) continue;
			JsonObject jMob = je.getAsJsonObject();
			
			TaskTame task = new TaskTame();
			task.idName = JsonHelper.GetString(jMob, "tame", "minecraft:horse");
			task.required = JsonHelper.GetNumber(jMob, "tames", 1).intValue();
			task.subtypes = !JsonHelper.GetBoolean(jMob, "exact", false);
			tList.add(task);
		}
		
		return tList.toArray(new ITask[0]);
    }
}
