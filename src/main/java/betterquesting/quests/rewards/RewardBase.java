package betterquesting.quests.rewards;

import net.minecraft.entity.player.EntityPlayer;
import betterquesting.client.GuiQuesting;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class RewardBase
{
	public abstract boolean canClaim(EntityPlayer player);
	
	public abstract void Claim(EntityPlayer player);
	
	public abstract void readFromJson(JsonObject json);
	
	public abstract void writeToJson(JsonObject json);
	
	@SideOnly(Side.CLIENT)
	public abstract void drawReward(GuiQuesting screen, int mx, int my, int posX, int posY, int sizeX, int sizeY);
	
	/**
	 * Used to manipulate the reward based on mouse actions. Can be used for things like choices
	 */
	public void onMouse(int mx, int my)
	{
	}
}
