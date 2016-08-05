package betterquesting.api.database;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import com.google.gson.JsonObject;
import betterquesting.api.quests.IQuestLineContainer;

public interface IQuestLineDatabase
{
	public int getUniqueID();
	
	public void addQuestLine(IQuestLineContainer questLine);
	public void deleteQuestLine(int lineId);
	
	public IQuestLineContainer getQuestLine(int lineId);
	public List<IQuestLineContainer> getAllQuestLines();
	
	public void syncAll();
	public void syncPlayer(EntityPlayerMP player);
	
	public JsonObject writeToJson(JsonObject json);
	public void readFromJson(JsonObject json);
}
