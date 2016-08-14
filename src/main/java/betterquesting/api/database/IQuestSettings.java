package betterquesting.api.database;

import com.google.gson.JsonObject;


public interface IQuestSettings extends IJsonSaveLoad<JsonObject>
{
	public boolean isEditMode();
	public boolean isHardcore();
	
	public void setEditMode(boolean state);
	public void setHardcore(boolean state);
	
	public void syncAll();
}
