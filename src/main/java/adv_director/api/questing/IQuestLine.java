package adv_director.api.questing;

import adv_director.api.misc.IDataSync;
import adv_director.api.misc.IJsonSaveLoad;
import adv_director.api.properties.IPropertyContainer;
import adv_director.api.storage.IRegStorageBase;
import com.google.gson.JsonObject;

public interface IQuestLine extends IDataSync, IJsonSaveLoad<JsonObject>, IRegStorageBase<Integer,IQuestLineEntry>
{
	public String getUnlocalisedName();
	public String getUnlocalisedDescription();
	
	// Defaults to the API if not used
	public void setParentDatabase(IQuestLineDatabase questDB);
	
	public IPropertyContainer getProperties();
	
	public int getQuestAt(int x, int y);
	
	public IQuestLineEntry createNewEntry();
}
