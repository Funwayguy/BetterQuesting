package betterquesting.api.database;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import betterquesting.api.quests.IQuestContainer;
import com.google.gson.JsonObject;

public interface IQuestDatabase
{
	public int getUniqueID();
	
	public void addQuest(IQuestContainer quest);
	public void deleteQuest(int quesId);
	
	public IQuestContainer getQuest(int questId);
	public List<IQuestContainer> getAllQuests();
	
	public void syncAll();
	public void syncPlayer(EntityPlayerMP player);
	
	public JsonObject writeToJson_Config(JsonObject json);
	public void readFromJson_Config(JsonObject json);
	
	public JsonObject writeToJson_Progress(JsonObject json);
	public void readFromJson_Progress(JsonObject json);
}
