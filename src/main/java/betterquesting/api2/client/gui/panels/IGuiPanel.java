package betterquesting.api2.client.gui.panels;

import java.util.List;
import betterquesting.api2.client.gui.misc.IGuiRect;

public interface IGuiPanel
{
	public IGuiRect getTransform();
	
	// Don't initialise anything with fixed positions or size in here. It's all relative
	public void initPanel();
	public void drawPanel(int mx, int my, float partialTick);
	
	public boolean onMouseClick(int mx, int my, int button);
	public boolean onMouseRelease(int mx, int my, int button);
	public boolean onMouseScroll(int mx, int my, int scroll);
	public void onKeyTyped(char c, int keycode);
	
	public List<String> getTooltip(int mx, int my);
}
