package betterquesting.api.quests;

import betterquesting.api.database.IDataSync;
import betterquesting.api.database.IJsonSaveLoad;
import betterquesting.api.database.IRegStorage;
import betterquesting.api.quests.properties.IQuestInfo;
import com.google.gson.JsonObject;

public interface IQuestLineContainer extends IDataSync, IJsonSaveLoad<JsonObject>, IRegStorage<IQuestLineEntry>
{
	public String getUnlocalisedName();
	public String getUnlocalisedDescription();
	
	public IQuestInfo getInfo();
	
	public int getQuestAt(int x, int y);
}
