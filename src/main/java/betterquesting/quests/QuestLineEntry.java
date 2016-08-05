package betterquesting.quests;

import com.google.gson.JsonObject;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.quests.IQuestLineEntry;
import betterquesting.api.utils.JsonHelper;

public class QuestLineEntry implements IQuestLineEntry
{
	private IQuestContainer quest;
	private int size = 0;
	private int posX = 0;
	private int posY = 0;
	
	public QuestLineEntry(JsonObject json)
	{
		this.readFromJson(json);
	}
	
	public QuestLineEntry(IQuestContainer quest, int x, int y)
	{
		this(quest, x, y, 24);
	}
	
	public QuestLineEntry(IQuestContainer quest, int x, int y, int size)
	{
		this.quest = quest;
		this.size = size;
		this.posX = x;
		this.posY = y;
	}
	
	@Override
	public IQuestContainer getQuest()
	{
		return quest;
	}
	
	@Override
	public int getSize()
	{
		return size;
	}
	
	@Override
	public int getPosX()
	{
		return posX;
	}
	
	@Override
	public int getPosY()
	{
		return posY;
	}
	
	@Override
	public void setPosition(int posX, int posY)
	{
		this.posX = posX;
		this.posY = posY;
	}
	
	@Override
	public void setSize(int size)
	{
		this.size = size;
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json)
	{
		json.addProperty("id", quest.getQuestID());
		json.addProperty("size", size);
		json.addProperty("x", posX);
		json.addProperty("y", posY);
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		quest = QuestDatabase.INSTANCE.getQuest(JsonHelper.GetNumber(json, "id", -1).intValue());
		size = JsonHelper.GetNumber(json, "size", 24).intValue();
		posX = JsonHelper.GetNumber(json, "x", 0).intValue();
		posY = JsonHelper.GetNumber(json, "y", 0).intValue();
	}
	
}
