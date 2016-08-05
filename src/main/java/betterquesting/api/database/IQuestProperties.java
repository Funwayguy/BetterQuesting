package betterquesting.api.database;

import net.minecraft.entity.player.EntityPlayerMP;
import com.google.gson.JsonObject;

public interface IQuestProperties
{
	public boolean isEditMode();
	public boolean isHardcore();
	
	public void setEditMode(boolean state);
	public void setHardcore(boolean state);
	
	public void syncAll();
	public void syncPlayer(EntityPlayerMP player);
	
	public JsonObject writeToJson(JsonObject json);
	public void readFromJson(JsonObject json);
}
