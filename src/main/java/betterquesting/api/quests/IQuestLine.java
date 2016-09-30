package betterquesting.api.quests;

import betterquesting.api.database.IDataSync;
import betterquesting.api.database.IJsonSaveLoad;
import betterquesting.api.database.IRegStorageBase;
import betterquesting.api.quests.properties.IPropertyContainer;
import com.google.gson.JsonObject;

public interface IQuestLine extends IDataSync, IJsonSaveLoad<JsonObject>, IRegStorageBase<Integer,IQuestLineEntry>
{
	public String getUnlocalisedName();
	public String getUnlocalisedDescription();
	
	public IPropertyContainer getProperties();
	
	public int getQuestAt(int x, int y);
}
