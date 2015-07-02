package betterquesting.quests.types;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import com.google.gson.JsonObject;

public abstract class QuestBase
{
	/**
	 * List of users that have completed this quest
	 */
	ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	
	/**
	 * Default unlocalised name for this quest type
	 * @return
	 */
	public abstract String getUnlocalisedName();
	
	/**
	 * Returns a localized version of the quest type name
	 */
	public String getDisplayName()
	{
		return StatCollector.translateToLocal(this.getUnlocalisedName());
	}
	
	/**
	 * Updates the questing logic based on the given player instance<br>
	 * WARNING: Keep processing to a minimum here as this is called through EntityLivingUpdate<br>
	 * NOTE: This is only run server side. Send custom packets if clientside action is required
	 * @param player
	 * @param quest
	 */
	public abstract void Update(EntityPlayer player);

	/**
	 * Fired when someone presses the detect button for the quest. Use this for item submissions or manual updates
	 * @param player
	 * @param quest
	 */
	public abstract void Detect(EntityPlayer player);
	
	public boolean isComplete(EntityPlayer player)
	{
		return completeUsers.contains(player.getUniqueID());
	}
	
	public void setCompletion(EntityPlayer player, boolean state)
	{
		if(state)
		{
			if(!completeUsers.contains(player.getUniqueID()))
			{
				completeUsers.add(player.getUniqueID());
			}
		} else
		{
			completeUsers.remove(player.getUniqueID());
		}
	}
	
	public abstract void writeToJson(JsonObject json);
	
	public abstract void readFromJson(JsonObject json);
	
	/**
	 * Used to draw additional info about the quest's requirements. Objects drawn should be made to fit within the given dimensions
	 * @param screen
	 * @param posX
	 * @param posY
	 * @param sizeX
	 * @param sizeY
	 */
	public void drawQuestInfo(GuiScreen screen, int posX, int posY, int sizeX, int sizeY){}
}
