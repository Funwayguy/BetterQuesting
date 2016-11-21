package betterquesting.api.questing;

import betterquesting.api.other.IDataSync;
import betterquesting.api.other.IJsonSaveLoad;
import betterquesting.api.registry.IRegStorageBase;
import com.google.gson.JsonArray;

public interface IQuestDatabase extends IRegStorageBase<Integer,IQuest>, IJsonSaveLoad<JsonArray>, IDataSync
{
	public IQuest createNew();
}
