package betterquesting.api.database;

import com.google.gson.JsonArray;
import betterquesting.api.quests.IQuestLineContainer;

public interface IQuestLineDatabase extends IRegStorage<IQuestLineContainer>, IJsonSaveLoad<JsonArray>, IDataSync
{
	/**
	 * Deletes quest from all quest lines
	 */
	public void removeQuest(int questID);
}
