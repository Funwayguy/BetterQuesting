package betterquesting.api2.client.gui.panels.content;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.IGuiLine;
import betterquesting.api2.client.gui.themes.ThemeRegistry;

public class PanelLine implements IGuiPanel
{
	/**
	 * Bounds aren't used in the drawing of the line, merely for determining draw order
	 */
	private final IGuiRect bounds = new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0);
	private final IGuiLine line;
	private final IGuiRect start;
	private final IGuiRect end;
	
	private ResourceLocation color;
	private int width = 1;
	
	public PanelLine(IGuiRect start, IGuiRect end, IGuiLine line, int width, ResourceLocation color)
	{
		this.start = start;
		this.end = end;
		this.line = line;
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
		line.drawLine(start, end, width, ThemeRegistry.INSTANCE.getColor(color), partialTick);
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
	public boolean onKeyTyped(char c, int keycode)
	{
		return false;
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		return null;
	}
	
}
