package adv_director.rw2.api.client.gui.misc;

public interface IGuiRect extends Comparable<IGuiRect>
{
	public int getX();
	public int getY();
	public int getWidth();
	public int getHeight();
	public int getDepth();
	
	public IGuiRect getParent();
	public void setParent(IGuiRect rect);
	
	public boolean contains(int x, int y);
	public void translate(int x, int y);
}
