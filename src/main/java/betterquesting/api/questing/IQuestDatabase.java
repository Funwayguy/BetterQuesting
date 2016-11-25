package betterquesting.api.questing;

import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.storage.IRegStorageBase;
import com.google.gson.JsonArray;

public interface IQuestDatabase extends IRegStorageBase<Integer,IQuest>, IJsonSaveLoad<JsonArray>, IDataSync
{
	public IQuest createNew();
}
