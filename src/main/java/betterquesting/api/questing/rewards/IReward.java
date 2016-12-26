package betterquesting.api.questing.rewards;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.questing.IQuest;
import com.google.gson.JsonObject;

public interface IReward extends IJsonSaveLoad<JsonObject>
{
	public String getUnlocalisedName();
	public ResourceLocation getFactoryID();
	
	public boolean canClaim(EntityPlayer player, IQuest quest);
	public void claimReward(EntityPlayer player, IQuest quest);
	
	public IJsonDoc getDocumentation();
	
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getRewardGui(int x, int y, int w, int h, IQuest quest);
	
	@Nullable
	@SideOnly(Side.CLIENT)
	public GuiScreen getRewardEditor(GuiScreen parent, IQuest quest);
}
