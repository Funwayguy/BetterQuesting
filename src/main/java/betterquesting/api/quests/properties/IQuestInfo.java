package betterquesting.api.quests.properties;

import betterquesting.api.database.IJsonSaveLoad;
import com.google.gson.JsonObject;

public interface IQuestInfo extends IJsonSaveLoad<JsonObject>
{
	public <T> T getProperty(IQuestProperty<T> prop);
	public <T> T getProperty(IQuestProperty<T> prop, T def);
	
	public boolean hasProperty(IQuestProperty<?> prop);
	
	public <T> void setProperty(IQuestProperty<T> prop, T value);
}