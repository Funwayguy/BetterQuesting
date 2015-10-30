package betterquesting.quests.rewards;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.json.GuiJsonObject;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class RewardBase
{
	public abstract String getUnlocalisedName();
	
	public String getDisplayName()
	{
		return StatCollector.translateToLocal(this.getUnlocalisedName());
	}
	
	public abstract boolean canClaim(EntityPlayer player, NBTTagCompound choiceData);
	
	public abstract void Claim(EntityPlayer player, NBTTagCompound choiceData);
	
	public abstract void readFromJson(JsonObject json);
	
	public abstract void writeToJson(JsonObject json);
	
	@SideOnly(Side.CLIENT)
	public NBTTagCompound GetChoiceData()
	{
		return new NBTTagCompound();
	}
	
	@SideOnly(Side.CLIENT)
	public abstract void drawReward(GuiQuesting screen, int mx, int my, int posX, int posY, int sizeX, int sizeY);
	
	/**
	 * Used to manipulate the reward based on mouse actions. Can be used for things like choices
	 * @param click 
	 */
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
