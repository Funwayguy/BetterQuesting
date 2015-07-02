package betterquesting.rewards;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RewardItem extends RewardBase
{
	ItemStack reward = new ItemStack(Blocks.stone);
	
	@Override
	public boolean canClaim(EntityPlayer player)
	{
		return player.inventory.getFirstEmptyStack() == -1;
	}

	@Override
	public void Claim(EntityPlayer player)
	{
		player.inventory.addItemStackToInventory(reward);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("unchecked")
	public List<String> getTooltip(EntityPlayer player)
	{
		return reward.getTooltip(player, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
	}

	@Override
	public void readFromJson(JsonObject json)
	{
	}

	@Override
	public void writeToJson(JsonObject json)
	{
	}

	@Override
	public void drawReward(GuiScreen screen, int posX, int posY, int sizeX, int sizeY)
	{
	}
}
