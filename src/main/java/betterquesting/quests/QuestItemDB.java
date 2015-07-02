package betterquesting.quests;

import java.io.File;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.utils.JsonIO;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;

/**
 * Manages all the ItemStacks that quests use
 */
public class QuestItemDB
{
	static NBTTagCompound questItemStorage = new NBTTagCompound();
	
	/**
	 * Retrieves an quest specific ItemStack under the given ID name
	 * @param quest
	 * @param entryName
	 * @return ItemStack
	 */
	public static ItemStack GetQuestItem(QuestInstance quest, String entryName)
	{
		return ItemStack.loadItemStackFromNBT(questItemStorage.getCompoundTag("quest_" + quest.questID).getCompoundTag(entryName));
	}
	
	/**
	 * Places an ItemStack into the quest specific database under the given ID name. ItemStack can be null to clear the entry
	 * @param quest
	 * @param entryName
	 * @param stack
	 */
	public static void SetQuestItem(QuestInstance quest, String entryName, ItemStack stack)
	{
		NBTTagCompound questDB = questItemStorage.getCompoundTag("quest_" + quest.questID);
		if(stack != null)
		{
			questDB.setTag(entryName, stack.writeToNBT(new NBTTagCompound()));
		} else
		{
			questDB.removeTag(entryName);
		}
		questItemStorage.setTag("quest_" + quest.questID, questDB);
	}
	
	public static void DeleteQuestData(int id)
	{
		questItemStorage.removeTag("quest_" + id);
	}
	
	public static void DeleteQuestData(QuestInstance quest)
	{
		questItemStorage.removeTag("quest_" + quest.questID);
	}
	
	public static void writeToFile(File file)
	{
		JsonObject jObj = NBTConverter.NBTtoJSON_Compound(questItemStorage, new JsonObject());
		JsonIO.WriteToFile(file, jObj);
	}
	
	public static void readFromFile(File file)
	{
		JsonObject jObj = JsonIO.ReadFromFile(file);
		questItemStorage = NBTConverter.JSONtoNBT_Object(jObj, new NBTTagCompound());
	}
}
