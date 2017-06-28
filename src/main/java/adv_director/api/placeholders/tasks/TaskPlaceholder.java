package adv_director.api.placeholders.tasks;

import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import adv_director.api.client.gui.misc.IGuiEmbedded;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.jdoc.IJsonDoc;
import adv_director.api.questing.IQuest;
import adv_director.api.questing.tasks.ITask;
import adv_director.api.utils.JsonHelper;
import com.google.gson.JsonObject;

public class TaskPlaceholder implements ITask
{
	private JsonObject jsonData = new JsonObject();
	
	public void setTaskData(JsonObject json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			jsonData.add("orig_data", json);
		} else if(saveType == EnumSaveType.PROGRESS)
		{
			jsonData.add("orig_prog", json);
		}
	}
	
	public JsonObject getTaskData(EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			return JsonHelper.GetObject(jsonData, "orig_data");
		} else if(saveType == EnumSaveType.PROGRESS)
		{
			return JsonHelper.GetObject(jsonData, "orig_prog");
		}
		
		return new JsonObject();
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			json.add("orig_data", JsonHelper.GetObject(jsonData, "orig_data"));
		} else if(saveType == EnumSaveType.PROGRESS)
		{
			json.add("orig_prog", JsonHelper.GetObject(jsonData, "orig_prog"));
		}
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			jsonData.add("orig_data", JsonHelper.GetObject(json, "orig_data"));
		} else if(saveType == EnumSaveType.PROGRESS)
		{
			jsonData.add("orig_prog", JsonHelper.GetObject(json, "orig_prog"));
		}
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.placeholder";
	}
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskPlaceholder.INSTANCE.getRegistryName();
	}
	
	@Override
	public void update(EntityPlayer player, IQuest quest)
	{
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
	}
	
	@Override
	public boolean isComplete(UUID uuid)
	{
		return false;
	}
	
	@Override
	public void setComplete(UUID uuid)
	{
	}
	
	@Override
	public void resetUser(UUID uuid)
	{
	}
	
	@Override
	public void resetAll()
	{
	}
	
	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
	
	@Override
	public IGuiEmbedded getTaskGui(int x, int y, int w, int h, IQuest quest)
	{
		return null;
	}
	
	@Override
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
	{
		return null;
	}
}
