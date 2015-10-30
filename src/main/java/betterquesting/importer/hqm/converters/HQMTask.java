package betterquesting.importer.hqm.converters;

import java.util.ArrayList;
import betterquesting.quests.tasks.TaskBase;
import com.google.gson.JsonObject;

public abstract class HQMTask
{
	public abstract ArrayList<TaskBase> Convert(JsonObject json);
}
