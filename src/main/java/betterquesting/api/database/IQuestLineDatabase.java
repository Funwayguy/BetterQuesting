package betterquesting.api.database;

import com.google.gson.JsonArray;
import betterquesting.api.quests.IQuestLine;

public interface IQuestLineDatabase extends IRegStorageBase<Integer,IQuestLine>, IJsonSaveLoad<JsonArray>, IDataSync
{
	/**
	 * Deletes quest from all quest lines
	 */
	public void removeQuest(int questID);
}
