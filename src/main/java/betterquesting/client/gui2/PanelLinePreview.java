package betterquesting.client.gui2;

import java.util.List;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.IGuiLine;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;

public class PanelLinePreview implements IGuiPanel
{
	/**
	 * Bounds aren't used in the drawing of the line, merely for determining draw order
	 */
	private final IGuiRect bounds;
	
	private final IGuiLine[] lines;
	private final PresetColor[] colors = new PresetColor[4];
	
	private final IGuiRect start;
	private final IGuiRect end;
	
	private int width = 1;
	
	public PanelLinePreview(IGuiRect start, IGuiRect end, int width, int drawOrder)
	{
		this.bounds = new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), drawOrder);
		this.start = start;
		this.end = end;
		this.width = width;
		
		this.lines = new IGuiLine[]
		{
			PresetLine.QUEST_LOCKED.getLine(),
			PresetLine.QUEST_UNLOCKED.getLine(),
			PresetLine.QUEST_PENDING.getLine(),
			PresetLine.QUEST_COMPLETE.getLine()
		};
		
		this.colors[0] = PresetColor.QUEST_LINE_LOCKED;
		this.colors[1] = PresetColor.QUEST_LINE_UNLOCKED;
		this.colors[2] = PresetColor.QUEST_LINE_PENDING;
		this.colors[3] = PresetColor.QUEST_LINE_COMPLETE;
	}
	
	@Override
	public IGuiRect getTransform()
	{
		return bounds;
	}
	
	@Override
	public void initPanel()
	{
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		int i = (int)(System.currentTimeMillis() / 1000L) % 4;
		lines[i].drawLine(start, end, width, colors[i].getColor(), partialTick);
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int button)
	{
		return false;
	}
	
	@Override
	public boolean onMouseRelease(int mx, int my, int button)
	{
		return false;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		return false;
	}
	
	@Override
	public void onKeyTyped(char c, int keycode)
	{
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		return null;
	}
}
