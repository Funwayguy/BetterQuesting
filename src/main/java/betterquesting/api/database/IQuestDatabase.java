package betterquesting.api.database;

import com.google.gson.JsonArray;
import net.minecraft.entity.player.EntityPlayerMP;
import betterquesting.api.quests.IQuestContainer;

public interface IQuestDatabase extends IRegStorage<IQuestContainer>, IJsonSaveLoad<JsonArray>
{
	public void syncAll();
	public void syncPlayer(EntityPlayerMP player);
}
