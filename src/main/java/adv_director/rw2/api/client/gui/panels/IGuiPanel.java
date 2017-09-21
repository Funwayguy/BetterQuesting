package adv_director.rw2.api.client.gui.panels;

import java.util.List;
import adv_director.rw2.api.client.gui.events.PanelEvent;
import adv_director.rw2.api.client.gui.misc.IGuiRect;

public interface IGuiPanel
{
	public IGuiRect getTransform();
	public void setTransform(IGuiRect trans);
	
	public void initPanel();
	public void drawPanel(int mx, int my, float partialTick);
	
	public boolean onMouseClick(int mx, int my, int click);
	public boolean onMouseScroll(int mx, int my, int scroll);
	public void onKeyTyped(char c, int keycode);
	
	public void onPanelEvent(PanelEvent event);
	
	public List<String> getTooltip(int mx, int my);
}
