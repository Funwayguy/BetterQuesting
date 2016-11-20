package betterquesting.api.questing;

import betterquesting.api.other.IJsonSaveLoad;
import com.google.gson.JsonObject;

public interface IQuestLineEntry extends IJsonSaveLoad<JsonObject>
{
	public int getSize();
	public int getPosX();
	public int getPosY();
	
	public void setPosition(int posX, int posY);
	public void setSize(int size);
}
