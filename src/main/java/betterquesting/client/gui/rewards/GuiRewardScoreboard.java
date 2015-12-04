package betterquesting.client.gui.rewards;

import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.quests.rewards.RewardScoreboard;

public class GuiRewardScoreboard extends GuiEmbedded
{
	RewardScoreboard reward;
	
	public GuiRewardScoreboard(RewardScoreboard reward, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.reward = reward;
	}

	@Override
	public void drawTask(int mx, int my, float partialTick)
	{
		screen.mc.fontRenderer.drawString(reward.score + (reward.value >= 0? " +" : " ") + reward.value, posX, posY, ThemeRegistry.curTheme().textColor().getRGB());
	}
}
