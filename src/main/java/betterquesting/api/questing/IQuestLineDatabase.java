package betterquesting.api.questing;

import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.storage.IRegStorageBase;
import com.google.gson.JsonArray;

public interface IQuestLineDatabase extends IRegStorageBase<Integer,IQuestLine>, IJsonSaveLoad<JsonArray>, IDataSync
{
	/**
	 * Deletes quest from all quest lines
	 */
	public void removeQuest(int lineID);
	
	public int getOrderIndex(int lineID);
	public void setOrderIndex(int lineID, int index);
	
	public IQuestLine createNew();
}
