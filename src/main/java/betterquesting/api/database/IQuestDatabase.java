package betterquesting.api.database;

import betterquesting.api.quests.IQuestContainer;
import com.google.gson.JsonArray;

public interface IQuestDatabase extends IRegStorage<IQuestContainer>, IJsonSaveLoad<JsonArray>, IDataSync
{
}
