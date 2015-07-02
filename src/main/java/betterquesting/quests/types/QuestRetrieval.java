package betterquesting.quests.types;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import com.google.gson.JsonObject;

public class QuestRetrieval extends QuestBase
{
	ArrayList<ItemStack> requiredItems = new ArrayList<ItemStack>();
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.quest.retrieval.name";
	}

	@Override
	public void Update(EntityPlayer player)
	{
	}

	@Override
	public void Detect(EntityPlayer player)
	{
		for(ItemStack stack : player.inventory.mainInventory)
		{
			if(stack == null)
			{
				
			}
		}
	}

	@Override
	public void writeToJson(JsonObject json)
	{
	}

	@Override
	public void readFromJson(JsonObject json)
	{
	}
}
