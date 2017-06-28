package adv_director.api.questing;

import adv_director.api.misc.IDataSync;
import adv_director.api.misc.IJsonSaveLoad;
import adv_director.api.storage.IRegStorageBase;
import com.google.gson.JsonArray;

public interface IQuestDatabase extends IRegStorageBase<Integer,IQuest>, IJsonSaveLoad<JsonArray>, IDataSync
{
	public IQuest createNew();
}
