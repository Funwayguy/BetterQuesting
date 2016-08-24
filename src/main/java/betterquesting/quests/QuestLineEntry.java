package betterquesting.quests;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.IQuestLineEntry;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;

public class QuestLineEntry implements IQuestLineEntry
{
	private int size = 0;
	private int posX = 0;
	private int posY = 0;
	
	public QuestLineEntry(JsonObject json)
	{
		this.readFromJson(json, EnumSaveType.CONFIG);
	}
	
	public QuestLineEntry(int x, int y)
	{
		this(x, y, 24);
	}
	
	public QuestLineEntry(int x, int y, int size)
	{
		this.size = size;
		this.posX = x;
		this.posY = y;
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
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		json.addProperty("size", size);
		json.addProperty("x", posX);
		json.addProperty("y", posY);
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		size = JsonHelper.GetNumber(json, "size", 24).intValue();
		posX = JsonHelper.GetNumber(json, "x", 0).intValue();
		posY = JsonHelper.GetNumber(json, "y", 0).intValue();
	}
	
}
