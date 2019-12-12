package betterquesting.api.placeholders.rewards;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class RewardPlaceholder implements IReward
{
	private CompoundNBT nbtSaved = new CompoundNBT();
	
	public void setRewardConfigData(CompoundNBT nbt)
	{
        nbtSaved = nbt;
	}
	
	public CompoundNBT getRewardConfigData()
	{
        return nbtSaved;
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt)
	{
		nbt.put("orig_data", nbtSaved);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(CompoundNBT nbt)
	{
		nbtSaved = nbt.getCompound("orig_data");
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.placeholder";
	}
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryRewardPlaceholder.INSTANCE.getRegistryName();
	}
	
	@Override
	public boolean canClaim(PlayerEntity player, DBEntry<IQuest> quest)
	{
		return false;
	}
	
	@Override
	public void claimReward(PlayerEntity player, DBEntry<IQuest> quest)
	{
	}
	
	@Override
	public IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest)
	{
		return null;
	}
	
	@Override
	public Screen getRewardEditor(Screen parent, DBEntry<IQuest> quest)
	{
		return null;
	}
}
