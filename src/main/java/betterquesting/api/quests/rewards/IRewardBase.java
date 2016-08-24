package betterquesting.api.quests.rewards;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.database.IJsonSaveLoad;
import betterquesting.api.quests.IQuestContainer;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IRewardBase extends IJsonSaveLoad<JsonObject>
{
	public String getUnlocalisedName();
	public ResourceLocation getFactoryID();
	
	public boolean canClaim(EntityPlayer player, IQuestContainer quest);
	public void claimReward(EntityPlayer player, IQuestContainer quest);
	
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getRewardGui(int x, int y, int w, int h, IQuestContainer quest);
	
	@SideOnly(Side.CLIENT)
	public GuiScreen getRewardEditor(GuiScreen parent, IQuestContainer quest);
}
