package betterquesting.api2.client.gui.themes;

import betterquesting.api.client.themes.ITheme;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.misc.DummyQuest;
import betterquesting.misc.DummyQuest.IMainQuery;

import java.util.UUID;

public class LegacyLineWrapper implements IGuiLine, IMainQuery
{
    private static final UUID dummyID = UUID.randomUUID();
    
	private final ITheme oldTheme;
	private final DummyQuest dummyQuest;
	
	private int lastWidth = 4;
 
	public LegacyLineWrapper(ITheme theme, EnumQuestState state)
    {
        this.oldTheme = theme;
        this.dummyQuest = new DummyQuest(state).setMainCallback(this);
    }
    
    @Override
    public void drawLine(IGuiRect start, IGuiRect end, int width, IGuiColor color, float partialTick)
    {
        this.lastWidth = width;
        
        float x1 = start.getX() + start.getWidth() / 2F;
        float y1 = start.getY() + start.getHeight() / 2F;
        float x2 = end.getX() + end.getWidth() / 2F;
        float y2 = end.getY() + end.getHeight() / 2F;
        
        oldTheme.getRenderer().drawLine(dummyQuest, dummyID, x1, y1, x2, y2, 0, 0, partialTick);
    }
    
    @Override
    public Boolean getMain()
    {
        return lastWidth > 4;
    }
}
