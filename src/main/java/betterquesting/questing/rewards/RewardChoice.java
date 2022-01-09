package betterquesting.questing.rewards;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.NBTReplaceUtil;
import betterquesting.client.gui2.rewards.PanelRewardChoice;
import betterquesting.questing.rewards.factory.FactoryRewardChoice;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.*;

public class RewardChoice implements IReward
{
	/**
	 * The selected reward index to be claimed.<br>
	 * Should only ever be used client side. NEVER onHit server
	 */
	public final List<BigItemStack> choices = new ArrayList<>();
	private final TreeMap<UUID,Integer> selected = new TreeMap<>();
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryRewardChoice.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.choice";
	}
	
	public int getSelecton(UUID uuid)
	{
		if(!selected.containsKey(uuid))
		{
			return -1;
		}
		
		return selected.get(uuid);
	}
	
	public void setSelection(UUID uuid, int value)
	{
		selected.put(uuid, value);
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, DBEntry<IQuest> quest)
	{
		if(!selected.containsKey(QuestingAPI.getQuestingUUID(player))) return false;
		
		int tmp = selected.get(QuestingAPI.getQuestingUUID(player));
		return choices.size() <= 0 || (tmp >= 0 && tmp < choices.size());
	}

	@Override
	public void claimReward(EntityPlayer player, DBEntry<IQuest> quest)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(choices.size() <= 0)
		{
			return;
		} else if(!selected.containsKey(playerID))
		{
			return;
		}
		
		int tmp = selected.get(playerID);
		
		if(tmp < 0 || tmp >= choices.size())
		{
            BetterQuesting.logger.log(Level.ERROR, "Choice reward was forcibly claimed with invalid choice", new IllegalStateException());
			return;
		}
		
		BigItemStack stack = choices.get(tmp);
		stack = stack == null? null : stack.copy();
		
		if(stack == null || stack.stackSize <= 0)
		{
            BetterQuesting.logger.log(Level.WARN, "Claimed reward choice was null or was 0 in size!");
			return;
		}
		
		for(ItemStack s : stack.getCombinedStacks())
		{
			if(s.getTagCompound() != null)
			{
				s.setTagCompound(NBTReplaceUtil.replaceStrings(s.getTagCompound(), "VAR_NAME", player.getName()));
				s.setTagCompound(NBTReplaceUtil.replaceStrings(s.getTagCompound(), "VAR_UUID", QuestingAPI.getQuestingUUID(player).toString()));
			}
			
			if(!player.inventory.addItemStackToInventory(s))
			{
				player.dropItem(s, true, false);
			}
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		choices.clear();
		NBTTagList cList = nbt.getTagList("choices", 10);
		for(int i = 0; i < cList.tagCount(); i++)
		{
			choices.add(JsonHelper.JsonToItemStack(cList.getCompoundTagAt(i)));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList rJson = new NBTTagList();
		for(BigItemStack stack : choices)
		{
			rJson.appendTag(JsonHelper.ItemStackToJson(stack, new NBTTagCompound()));
		}
		nbt.setTag("choices", rJson);
		return nbt;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest)
	{
	    return new PanelRewardChoice(rect, quest, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getRewardEditor(GuiScreen screen, DBEntry<IQuest> quest)
	{
		return null;
	}
}
