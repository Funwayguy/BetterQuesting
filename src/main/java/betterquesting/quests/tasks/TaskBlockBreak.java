package betterquesting.quests.tasks;

import java.util.HashMap;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.quests.tasks.advanced.AdvancedTaskBase;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;

public class TaskBlockBreak extends AdvancedTaskBase
{
	HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	Block targetBlock = Blocks.stone;
	int targetMeta = 0;
	int targetNum = 1;
	
	BigItemStack dispStack;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.task.block.break";
	}
	
	@Override
	public void onBlockBreak(EntityPlayer player, Block block, int metadata, int x, int y, int z)
	{
		Integer progress = userProgress.get(player.getUniqueID());
		progress = progress == null? 0 : progress;
		
		if(block == targetBlock && metadata == targetMeta)
		{
			progress++;
			userProgress.put(player.getUniqueID(), progress);
			
			if(progress >= targetNum)
			{
				this.completeUsers.add(player.getUniqueID());
			}
		}
	}
	
	@Override
	public void drawQuestInfo(GuiQuesting screen, int mouseX, int mouseY, int posX, int posY, int sizeX, int sizeY)
	{
		if(dispStack == null)
		{
			dispStack = new BigItemStack(targetBlock, 1, targetMeta);
		}
		
		RenderUtils.RenderItemStack(screen.mc, dispStack.getBaseStack(), posX, posY, dispStack.stackSize > 0? "" : "" + dispStack.stackSize);
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		json.addProperty("blockID", Block.blockRegistry.getNameForObject(targetBlock));
		json.addProperty("blockMeta", targetMeta);
		json.addProperty("amount", targetNum);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		dispStack = null;
		targetBlock = (Block)Block.blockRegistry.getObject(JsonHelper.GetString(json, "blockID", "minecraft:stone"));
		targetMeta = JsonHelper.GetNumber(json, "blockMeta", 0).intValue();
		targetNum = JsonHelper.GetNumber(json, "amount", 1).intValue();
	}
}
