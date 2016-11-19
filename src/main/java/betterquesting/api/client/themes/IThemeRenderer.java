package betterquesting.api.client.themes;

import betterquesting.api.quests.IQuest;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IThemeRenderer
{
	public void drawLine(IQuest quest, float x1, float y1, float x2, float y2, int mx, int my, float partialTick);
	public void drawIcon(IQuest quest, float px, float py, float sx, float sy, int mx, int my, float partialTick);
}
