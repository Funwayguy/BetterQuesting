package betterquesting.api.placeholders;

import java.util.UUID;
import betterquesting.api.client.themes.IThemeRenderer;
import betterquesting.api.questing.IQuest;

public final class ThemeRenderDummy implements IThemeRenderer
{
	public static final ThemeRenderDummy INSTANCE = new ThemeRenderDummy();
	
	@Override
	public void drawLine(IQuest quest, UUID playerID, float x1, float y1, float x2, float y2, int mx, int my, float partialTick)
	{
	}
	
	@Override
	public void drawIcon(IQuest quest, UUID playerID, float px, float py, float sx, float sy, int mx, int my, float partialTick)
	{
	}
	
	@Override
	public void drawThemedPanel(int x, int y, int h, int w)
	{
	}
}
