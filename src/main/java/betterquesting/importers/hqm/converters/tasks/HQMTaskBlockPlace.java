package betterquesting.importers.hqm.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.importers.hqm.HQMUtilities;
import betterquesting.questing.tasks.TaskInteractItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public class HQMTaskBlockPlace
{
    public ITask[] convertTask(JsonObject json)
    {
		List<ITask> tList = new ArrayList<>();
		
		for(JsonElement je : JsonHelper.GetArray(json, "blocks"))
		{
            if(!(je instanceof JsonObject)) continue;
			JsonObject jObj = je.getAsJsonObject();
			
			TaskInteractItem task = new TaskInteractItem();
            BigItemStack stack = HQMUtilities.HQMStackT1(JsonHelper.GetObject(jObj, "item"));
            task.targetItem = new BigItemStack(stack.writeToNBT(new NBTTagCompound()));
            task.required = JsonHelper.GetNumber(jObj, "required", 1).intValue();
			tList.add(task);
		}
		
		return tList.toArray(new ITask[0]);
    }
}
