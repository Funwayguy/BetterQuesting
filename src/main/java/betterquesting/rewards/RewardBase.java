package betterquesting.rewards;

import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class RewardBase
{
	public abstract boolean canClaim(EntityPlayer player);
	
	public abstract void Claim(EntityPlayer player);
	
	@SideOnly(Side.CLIENT)
	public abstract List<String> getTooltip(EntityPlayer player);
	
	public abstract void readFromJson(JsonObject json);
	
	public abstract void writeToJson(JsonObject json);
	
	@SideOnly(Side.CLIENT)
	public abstract void drawReward(GuiScreen screen, int posX, int posY, int sizeX, int sizeY);
}
