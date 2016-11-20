package betterquesting.api.questing;

import com.google.gson.JsonArray;
import betterquesting.api.other.IDataSync;
import betterquesting.api.other.IJsonSaveLoad;
import betterquesting.api.registry.IRegStorageBase;

public interface IQuestLineDatabase extends IRegStorageBase<Integer,IQuestLine>, IJsonSaveLoad<JsonArray>, IDataSync
{
	/**
	 * Deletes quest from all quest lines
	 */
	public void removeQuest(int questID);
}
