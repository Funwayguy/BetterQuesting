package betterquesting.api2.client.gui.panels;

public interface IGuiCanvas extends IGuiPanel
{
	void addPanel(IGuiPanel panel);
	boolean removePanel(IGuiPanel panel);
	
	/**
	 * Removes all children and resets the canvas to its initial blank state
	 */
	void resetCanvas();
}