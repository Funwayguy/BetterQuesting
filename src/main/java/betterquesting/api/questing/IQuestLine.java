package betterquesting.api.questing;

import betterquesting.api.other.IDataSync;
import betterquesting.api.other.IJsonSaveLoad;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.registry.IRegStorageBase;
import com.google.gson.JsonObject;

public interface IQuestLine extends IDataSync, IJsonSaveLoad<JsonObject>, IRegStorageBase<Integer,IQuestLineEntry>
{
	public String getUnlocalisedName();
	public String getUnlocalisedDescription();
	
	public IPropertyContainer getProperties();
	
	public int getQuestAt(int x, int y);
}
