package betterquesting.api2.client.gui.misc;

public interface IGuiRect extends Comparable<IGuiRect>
{
	int getX();
	int getY();
	int getWidth();
	int getHeight();
	int getDepth();
	
	IGuiRect getParent();
	void setParent(IGuiRect rect);
	
	boolean contains(int x, int y);
	
	// I'll probably re-implement this at a later date when it serves more of a purpose
	//void translate(int x, int y);
}
