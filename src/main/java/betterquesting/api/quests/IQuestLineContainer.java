package betterquesting.api.quests;

import java.util.List;
import com.google.gson.JsonObject;

public interface IQuestLineContainer
{
	public String getUnlocalisedName();
	public String getUnlocalisedDescription();
	
	public IQuestLineEntry getQuestEntry(int questId);
	
	public List<IQuestLineEntry> getAllQuests();
	
	public JsonObject writeToJson(JsonObject json);
	public void readFromJson(JsonObject json);
}
