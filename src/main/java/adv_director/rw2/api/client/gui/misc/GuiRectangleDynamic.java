package adv_director.rw2.api.client.gui.misc;


public class GuiRectangleDynamic extends GuiRectangle
{
	public int offX = 0;
	public int offY = 0;

	public int offW = 0;
	public int offH = 0;
	
	public GuiRectangleDynamic(int x, int y, int w, int h)
	{
		this(x, y, w, h, 0);
	}
	
	public GuiRectangleDynamic(int x, int y, int w, int h, int d)
	{
		super(x, y, w, h, d);
	}
	
	// Mainly to convert a GuiTransform to an immutable version
	public GuiRectangleDynamic(IGuiRect rect)
	{
		this(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), rect.getDepth());
	}
	
	@Override
	public int getX()
	{
		return super.getX() + offX;
	}

	@Override
	public int getY()
	{
		return super.getY() + offY;
	}

	@Override
	public int getWidth()
	{
		return super.getWidth() + offW;
	}

	@Override
	public int getHeight()
	{
		return super.getHeight() + offH;
	}
}
