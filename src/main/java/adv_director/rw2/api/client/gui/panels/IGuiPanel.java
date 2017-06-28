package adv_director.rw2.api.client.gui.panels;

import java.util.List;
import org.lwjgl.util.Rectangle;
import adv_director.rw2.api.client.gui.events.IPanelEvent;

public interface IGuiPanel
{
	public IGuiPanel getParentPanel();
	public void setParentPanel(IGuiPanel panel);
	
	public void initPanel();
	public void updateBounds(Rectangle bounds);
	public Rectangle getBounds();
	public void drawPanel(int mx, int my, float partialTick);
	
	public boolean onMouseClick(int mx, int my, int click);
	public boolean onMouseScroll(int mx, int my, int scroll);
	public void onKeyTyped(char c, int keycode);
	public void onPanelEvent(IPanelEvent event);
	
	public List<String> getTooltip(int mx, int my);
}
