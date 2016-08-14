package betterquesting.api.quests;

import betterquesting.api.database.IJsonSaveLoad;
import com.google.gson.JsonObject;

public interface IQuestSound extends IJsonSaveLoad<JsonObject>
{
	public String getUnlockSound();
	public String getUpdateSound();
	public String getCompleteSound();
}
