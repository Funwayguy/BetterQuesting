package betterquesting.api.client.gui.lists;

public interface IScrollingEntry
{
	public void drawBackground(int mx, int my, int px, int py, int width);
	public void drawForeground(int mx, int my, int px, int py, int width);
	
	public void onMouseClick(int mx, int my, int px, int py, int click, int index);
	
	public int getHeight();
	public boolean canDrawOutsideBox(boolean isForeground);
}
