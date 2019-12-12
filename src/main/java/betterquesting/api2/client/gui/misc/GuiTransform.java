package betterquesting.api2.client.gui.misc;

import betterquesting.abs.misc.GuiAnchor;

public final class GuiTransform implements IGuiRect
{
	private IGuiRect parent;
	private final GuiAnchor anchor;
	private final GuiPadding padding;
	private int drawOrder;
	
	public GuiTransform()
	{
		this(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0);
	}
	
	public GuiTransform(GuiAnchor anchor)
	{
		this(anchor, new GuiPadding(0, 0, 0, 0), 0);
	}
	
	public GuiTransform(GuiAnchor anchor, int xOff, int yOff, int width, int height, int order)
	{
		this(anchor.copy(), new GuiPadding(xOff, yOff, -xOff - width, -yOff - height), order);
	}
	
	public GuiTransform(GuiAnchor anchor, GuiPadding padding, int depth)
	{
		//this.anchor = anchor;
		this.padding = padding;
		this.drawOrder = depth;
		
		float l = Math.min(anchor.getX(), anchor.getZ());
		float r = Math.max(anchor.getX(), anchor.getZ());
		float t = Math.min(anchor.getY(), anchor.getW());
		float b = Math.max(anchor.getY(), anchor.getW());
		
		this.anchor = new GuiAnchor(l, t, r, b);
	}
	
	public GuiTransform copy()
    {
        GuiTransform trans = new GuiTransform(anchor.copy(), padding.copy(), drawOrder);
        trans.setParent(this.parent);
        return trans;
    }
	
	public GuiPadding getPadding()
	{
		return this.padding;
	}
	
	public GuiAnchor getAnchor()
	{
		return this.anchor;
	}
	
	public void setDrawDepth(int order)
	{
		this.drawOrder = order;
	}
	
	@Override
	public int getX()
	{
		int i = parent == null ? 0 : (parent.getX() + (int)Math.ceil(parent.getWidth() * this.anchor.getX()));
		return i + padding.getLeft();
	}
	
	@Override
	public int getY()
	{
		int i = parent == null ? 0 : (parent.getY() + (int)Math.ceil(parent.getHeight() * this.anchor.getY()));
		return i + padding.getTop();
	}
	
	@Override
	public int getWidth()
	{
		int i = parent == null ? 0 : (int)Math.ceil(parent.getWidth() * (this.anchor.getZ() - this.anchor.getX()));
		return i - (padding.getRight() + padding.getLeft());
	}
	
	@Override
	public int getHeight()
	{
		int i = parent == null ? 0 : (int)Math.ceil(parent.getHeight() * (this.anchor.getW() - this.anchor.getY()));
		return i - (padding.getBottom() + padding.getTop());
	}
	
	@Override
	public int getDepth()
	{
		return this.drawOrder;
	}
	
	@Override
	public IGuiRect getParent()
	{
		return parent;
	}
	
	@Override
	public void setParent(IGuiRect rect)
	{
		this.parent = rect;
	}
	
	@Override
	public boolean contains(int x3, int y3)
	{
		int x1 = getX();
		int y1 = getY();
		int w = getWidth();
		int h = getHeight();
		int x2 = x1 + w;
		int y2 = y1 + h;
		return x3 >= x1 && x3 < x2 && y3 >= y1 && y3 < y2;
	}

	@Override
	public int compareTo(IGuiRect o)
	{
		return (int)Math.signum(o.getDepth() - drawOrder);
	}
}
