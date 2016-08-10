package betterquesting.api.quests;

import com.google.gson.JsonObject;

public interface IQuestLineEntry
{
	public int getQuestID();
	public int getSize();
	public int getPosX();
	public int getPosY();
	
	public void setPosition(int posX, int posY);
	public void setSize(int size);
	
	public JsonObject writeToJson(JsonObject json);
	public void readFromJson(JsonObject json);
}
