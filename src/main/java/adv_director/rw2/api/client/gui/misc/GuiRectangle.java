package adv_director.rw2.api.client.gui.misc;

public class GuiRectangle implements IGuiRect
{
	public static final IGuiRect ZERO = new GuiRectangle(0, 0, 0, 0);
	
	public final int x, y, w, h, d;
	private IGuiRect parent = null;
	
	public GuiRectangle(int x, int y, int w, int h)
	{
		this(x, y, w, h, 0);
	}
	
	public GuiRectangle(int x, int y, int w, int h, int d)
	{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.d = d;
	}
	
	// Mainly to convert a GuiTransform to an immutable version
	public GuiRectangle(IGuiRect rect)
	{
		this(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), rect.getDepth());
	}

	@Override
	public int getX()
	{
		return x + (parent == null? 0 : parent.getX());
	}

	@Override
	public int getY()
	{
		return y + (parent == null? 0 : parent.getY());
	}

	@Override
	public int getWidth()
	{
		return w;
	}

	@Override
	public int getHeight()
	{
		return h;
	}
	
	@Override
	public int getDepth()
	{
		return d;
	}
	
	@Override
	public IGuiRect getParent()
	{
		return parent;
	}
	
	@Override
	public void setParent(IGuiRect rect)
	{
		if(this == ZERO)
		{
			return;
		}
		
		this.parent = rect;
	}
	
	@Override
	public boolean contains(int x, int y)
	{
		int x1 = this.x;
		int x2 = this.x + this.w;
		int y1 = this.y;
		int y2 = this.y + this.h;
		return x >= x1 && x < x2 && y >= y1 && y < y2;
	}
	
	@Override
	public int compareTo(IGuiRect o)
	{
		return (int)Math.signum(o.getDepth() - d);
	}
}
