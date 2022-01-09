package betterquesting.importers.hqm.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.questing.tasks.TaskAdvancement;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class HQMTaskAdvancement
{
    public ITask[] convertTask(JsonObject json)
    {
        List<ITask> tasks = new ArrayList<>();
        
        for(JsonElement je : JsonHelper.GetArray(json, "advancements"))
        {
            if(je == null || !je.isJsonObject()) continue;
            JsonObject jAdv = je.getAsJsonObject();
            TaskAdvancement taskAdv = new TaskAdvancement();
            taskAdv.advID = new ResourceLocation(JsonHelper.GetString(jAdv, "adv_name", ""));
            tasks.add(taskAdv);
        }
        
        return tasks.toArray(new ITask[0]);
    }
}
