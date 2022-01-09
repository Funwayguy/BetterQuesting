package betterquesting.importers.hqm.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.importers.hqm.HQMQuestImporter;
import betterquesting.importers.hqm.converters.HQMRep;
import betterquesting.questing.tasks.TaskScoreboard;
import betterquesting.questing.tasks.TaskScoreboard.ScoreOperation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class HQMTaskReputaion
{
    public ITask[] convertTask(JsonObject json)
    {
        List<ITask> tasks = new ArrayList<>();
        
        for(JsonElement je : JsonHelper.GetArray(json, "reputation"))
        {
            if(!(je instanceof JsonObject)) continue;
            JsonObject jRep = je.getAsJsonObject();
            
            String repId;
            JsonElement jid = jRep.get("reputation");
            if(jid == null || !jid.isJsonPrimitive()) continue;
            if(jid.getAsJsonPrimitive().isString())
            {
                repId = jid.getAsString();
            } else
            {
                repId = jid.getAsNumber().toString();
            }
    
            HQMRep repObj = HQMQuestImporter.INSTANCE.reputations.get(repId);
            if(repObj == null) continue;
            
            int markA = repObj.getMarker(JsonHelper.GetNumber(jRep, "lower", -1).intValue());
            int markB = repObj.getMarker(JsonHelper.GetNumber(jRep, "upper", -1).intValue());
            boolean invert = JsonHelper.GetBoolean(jRep, "inverted", false);
            
            TaskScoreboard task = new TaskScoreboard();
            task.scoreName = repObj.rName.replaceAll(" ", "_");
            task.scoreDisp = repObj.rName;
            task.type = "dummy";
            
            if(invert)
            {
                task.operation = ScoreOperation.LESS_THAN;
                task.target = markA;
            } else
            {
                task.operation = ScoreOperation.MORE_OR_EQUAL;
                task.target = markB;
            }
            
            tasks.add(task);
        }
        
        return tasks.toArray(new ITask[0]);
    }
}
