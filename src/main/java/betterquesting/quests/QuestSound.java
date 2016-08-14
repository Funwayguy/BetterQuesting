package betterquesting.quests;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.IQuestSound;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;

public class QuestSound implements IQuestSound
{
	private String sndUnlocked = "random.levelup";
	private String sndUpdated = "random.levelup";
	private String sndCompleted = "random.levelup";
	
	public QuestSound()
	{
	}
	
	public QuestSound(JsonObject json)
	{
		this.readFromJson(json, EnumSaveType.CONFIG);
	}
	
	@Override
	public String getUnlockSound()
	{
		return sndUnlocked;
	}
	
	@Override
	public String getUpdateSound()
	{
		return sndUpdated;
	}
	
	@Override
	public String getCompleteSound()
	{
		return sndCompleted;
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		json.addProperty("complete", sndCompleted);
		json.addProperty("update", sndUpdated);
		json.addProperty("unlock", sndUnlocked);
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		this.sndCompleted = JsonHelper.GetString(json, "complete", "random.levelup");
		this.sndUpdated = JsonHelper.GetString(json, "update", "random.levelup");
		this.sndUnlocked = JsonHelper.GetString(json, "unlock", "random.click");
	}
}
