package betterquesting.api.database;

import com.google.gson.JsonArray;
import betterquesting.api.quests.IQuestLineContainer;

public interface IQuestLineDatabase extends IRegStorage<IQuestLineContainer>, IJsonSaveLoad<JsonArray>
{
	public void syncDatabase();
}
