package betterquesting.quests;

import betterquesting.api.database.IQuestProperties;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;

public class QuestProperties implements IQuestProperties
{
	public static final QuestProperties INSTANCE = new QuestProperties();
	
	private boolean editMode = true;
	private boolean hardcore = false;
	
	private QuestProperties()
	{
		
	}
	
	@Override
	public boolean isEditMode()
	{
		return editMode;
	}
	
	@Override
	public boolean isHardcore()
	{
		return hardcore;
	}
	
	@Override
	public void setEditMode(boolean state)
	{
		editMode = state;
	}
	
	@Override
	public void setHardcore(boolean state)
	{
		hardcore = state;
	}
	
	@Override
	public void syncAll()
	{
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		json.addProperty("editMode", editMode);
		json.addProperty("hardcore", hardcore);
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		editMode = JsonHelper.GetBoolean(json, "editMode", true);
		hardcore = JsonHelper.GetBoolean(json, "hardcore", false);
	}
}
