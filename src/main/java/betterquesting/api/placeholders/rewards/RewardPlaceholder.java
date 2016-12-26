package betterquesting.api.placeholders.rewards;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonObject;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.utils.JsonHelper;

public class RewardPlaceholder implements IReward
{
	private JsonObject jsonSaved = new JsonObject();
	
	public void setRewardData(JsonObject json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			jsonSaved = json;
		}
	}
	
	public JsonObject getRewardData(EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			return jsonSaved;
		}
		
		return new JsonObject();
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		json.add("orig_data", jsonSaved);
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		jsonSaved = JsonHelper.GetObject(json, "orig_data");
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
	public IGuiEmbedded getRewardGui(int x, int y, int w, int h, IQuest quest)
	{
		return null;
	}
	
	@Override
	public GuiScreen getRewardEditor(GuiScreen parent, IQuest quest)
	{
		return null;
	}
}
