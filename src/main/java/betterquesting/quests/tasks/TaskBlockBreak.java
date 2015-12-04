package betterquesting.quests.tasks;

import java.util.HashMap;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.gui.tasks.GuiTaskBlock;
import betterquesting.quests.tasks.advanced.AdvancedTaskBase;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonObject;

public class TaskBlockBreak extends AdvancedTaskBase
{
	HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	public Block targetBlock = Blocks.log;
	public int targetMeta = -1;
	public int targetNum = 1;
	public boolean oreDict = true;
	
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
	public void writeToJson(JsonObject json)
	{
		json.addProperty("blockID", Block.blockRegistry.getNameForObject(targetBlock));
		json.addProperty("blockMeta", targetMeta);
		json.addProperty("amount", targetNum);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		targetBlock = (Block)Block.blockRegistry.getObject(JsonHelper.GetString(json, "blockID", "minecraft:log"));
		targetBlock = targetBlock != null? targetBlock : Blocks.log;
		targetMeta = JsonHelper.GetNumber(json, "blockMeta", -1).intValue();
		targetNum = JsonHelper.GetNumber(json, "amount", 1).intValue();
	}

	@Override
	public void ResetProgress(UUID uuid)
	{
		completeUsers.remove(uuid);
		userProgress.remove(uuid);
	}

	@Override
	public void ResetAllProgress()
	{
		completeUsers.clear();
		userProgress.clear();
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskBlock(this, screen, posX, posY, sizeX, sizeY);
	}
}
