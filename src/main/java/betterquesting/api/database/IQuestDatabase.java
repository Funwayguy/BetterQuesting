package betterquesting.api.database;

import betterquesting.api.quests.IQuest;
import com.google.gson.JsonArray;

public interface IQuestDatabase extends IRegStorageBase<Integer,IQuest>, IJsonSaveLoad<JsonArray>, IDataSync
{
}
