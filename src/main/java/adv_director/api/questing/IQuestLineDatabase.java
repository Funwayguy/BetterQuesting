package adv_director.api.questing;

import adv_director.api.misc.IDataSync;
import adv_director.api.misc.IJsonSaveLoad;
import adv_director.api.storage.IRegStorageBase;
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
