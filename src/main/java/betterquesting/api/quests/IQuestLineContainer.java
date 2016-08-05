package betterquesting.api.quests;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import com.google.gson.JsonObject;

public interface IQuestLineContainer
{
	public String getUnlocalisedName();
	public int getQuestLineID();
	
	public void addQuestEntry(IQuestLineEntry entry);
	public IQuestLineEntry getQuestEntry(int questId);
	
	public List<IQuestLineEntry> getAllQuests();
	
	public void syncAll();
	public void syncPlayer(EntityPlayerMP player);
	
	public JsonObject writeToJson(JsonObject json);
	public void readFromJson(JsonObject json);
}
