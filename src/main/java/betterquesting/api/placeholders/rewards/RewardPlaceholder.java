package betterquesting.api.placeholders.rewards;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class RewardPlaceholder implements IReward
{
	private NBTTagCompound nbtSaved = new NBTTagCompound();
	
	public void setRewardData(NBTTagCompound nbt, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			nbtSaved = nbt;
		}
	}
	
	public NBTTagCompound getRewardData(EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			return nbtSaved;
		}
		
		return new NBTTagCompound();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return nbt;
		}
		
		nbt.setTag("orig_data", nbtSaved);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		nbtSaved = nbt.getCompoundTag("orig_data");
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
	public boolean canClaim(EntityPlayer player, IQuest quest)
	{
		return false;
	}
	
	@Override
	public void claimReward(EntityPlayer player, IQuest quest)
	{
	}
	
	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
	
	@Override
	public IGuiPanel getRewardGui(IGuiRect rect, IQuest quest)
	{
		return null;
	}
	
	@Override
	public GuiScreen getRewardEditor(GuiScreen parent, IQuest quest)
	{
		return null;
	}
}
