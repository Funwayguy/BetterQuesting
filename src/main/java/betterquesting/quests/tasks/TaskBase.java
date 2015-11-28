package betterquesting.quests.tasks;

import java.util.ArrayList;
import java.util.UUID;
import org.apache.logging.log4j.Level;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class TaskBase
{
	/**
	 * List of users that have completed this quest
	 */
	protected ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	
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
	public void Update(EntityPlayer player){}

	/**
	 * Fired when someone presses the detect button for the quest (can be used occasionally in Update to auto-detect). Use this for item submissions or manual updates
	 * @param player
	 * @param quest
	 */
	public void Detect(EntityPlayer player){}
	
	/**
	 * Called by repeatable quests to reset progress for the next attempt
	 */
	public abstract void ResetProgress(UUID uuid);
	
	/**
	 * Clear all progress for all users
	 */
	public abstract void ResetAllProgress();
	
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
	
	public void writeToJson(JsonObject json)
	{
		JsonArray jArray = new JsonArray();
		for(UUID uuid : completeUsers)
		{
			jArray.add(new JsonPrimitive(uuid.toString()));
		}
		json.add("completeUsers", jArray);
	}
	
	public void readFromJson(JsonObject json)
	{
		completeUsers.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "completeUsers"))
		{
			if(entry == null || !entry.isJsonPrimitive())
			{
				continue;
			}
			
			try
			{
				completeUsers.add(UUID.fromString(entry.getAsString()));
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID '" + entry.getAsString() + "' for task", e);
			}
		}
	}
	
	/**
	 * Used to draw additional info about the quest's requirements. Objects drawn should be made to fit within the given dimensions
	 * @param screen
	 * @param posX
	 * @param posY
	 * @param sizeX
	 * @param sizeY
	 */
	@SideOnly(Side.CLIENT)
	public abstract void drawQuestInfo(GuiQuesting screen, int mouseX, int mouseY, int posX, int posY, int sizeX, int sizeY);
	
	@SideOnly(Side.CLIENT)
	public void MousePressed(GuiQuesting screen, int mx, int my, int posX, int posY, int sizeX, int sizeY, int click)
	{
	}
	
	/**
	 * Returns a new editor screen for this Reward type to edit the given data
	 */
	@SideOnly(Side.CLIENT)
	public GuiScreen GetEditor(GuiScreen parent, JsonObject data)
	{
		return new GuiJsonObject(parent, data);
	}
}
