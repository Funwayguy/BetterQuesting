package betterquesting.client.gui.editors.rewards;

import net.minecraft.client.gui.GuiScreen;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import com.google.gson.JsonObject;

public class GuiRewardEditDefault extends GuiScreenThemed
{
	public final IReward reward;
	public final JsonObject json;
	public boolean isDone = false;
	
	public GuiRewardEditDefault(GuiScreen parent, IReward reward)
	{
		super(parent, reward.getUnlocalisedName());
		this.reward = reward;
		this.json = reward.writeToJson(new JsonObject(), EnumSaveType.CONFIG);
		this.isDone = false;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		if(!isDone)
		{
			this.isDone = true;
			this.mc.displayGuiScreen(new GuiJsonObject(this, json, null));
		} else
		{
			this.reward.readFromJson(json, EnumSaveType.CONFIG);
			this.mc.displayGuiScreen(parent);
		}
	}
}
