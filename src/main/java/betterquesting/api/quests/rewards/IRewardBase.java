package betterquesting.api.quests.rewards;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.quests.IQuestContainer;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IRewardBase
{
	public String getUnlocalisedName();
	public ResourceLocation getFactoryID();
	
	public boolean canClaim(EntityPlayer player, IQuestContainer quest);
	public void claimReward(EntityPlayer player, IQuestContainer quest);
	
	public JsonObject writeToJson(JsonObject json);
	public void readFromJson(JsonObject json);
	
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getRewardGui(int sizeX, int sizeY, IQuestContainer quest);
	
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getRewardEditor(int sizeX, int sizeY, IQuestContainer quest);
}
