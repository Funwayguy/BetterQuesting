package betterquesting.api.questing.rewards;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public interface IReward extends INBTSaveLoad<CompoundNBT>
{
	String getUnlocalisedName();
	ResourceLocation getFactoryID();
	
	boolean canClaim(PlayerEntity player, DBEntry<IQuest> quest);
	void claimReward(PlayerEntity player, DBEntry<IQuest> quest);
	
	@OnlyIn(Dist.CLIENT)
    IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest);
	
	@Nullable
	@OnlyIn(Dist.CLIENT)
    Screen getRewardEditor(Screen parent, DBEntry<IQuest> quest);
}
