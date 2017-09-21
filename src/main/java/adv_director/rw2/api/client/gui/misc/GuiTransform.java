package adv_director.rw2.api.client.gui.misc;

import org.lwjgl.util.vector.ReadableVector4f;
import org.lwjgl.util.vector.Vector4f;

public final class GuiTransform implements IGuiRect
{
	private IGuiRect parentTranform = GuiRectangle.ZERO;
	private final Vector4f anchor = new Vector4f(0F, 0F, 1F, 1F);
	private final GuiPadding padding = new GuiPadding(0, 0, 0, 0);
	private int drawDepth = 0;
	
	public GuiTransform()
	{
		this(new Vector4f(0F, 0F, 1F, 1F), new GuiPadding(0, 0, 1, 1), 0);
	}
	
	public GuiTransform(IGuiRect rect)
	{
		this(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), rect.getDepth());
	}
	
	public GuiTransform(int x, int y, int w, int h, int z)
	{
		this(GuiAlign.TOP_LEFT, new GuiPadding(x, y, -w, -h), z);
	}
	
	public GuiTransform(ReadableVector4f anchor, GuiPadding padding, int depth)
	{
		this.setAnchor(anchor);
		this.setPadding(padding);
		this.drawDepth = depth;
	}
	
	public void setPadding(GuiPadding padding)
	{
		this.setPadding(padding.getLeft(), padding.getTop(), padding.getRight(), padding.getBottom());
	}
	
	public void setPadding(int left, int top, int right, int bottom)
	{
		int l = Math.min(left, right);
		int r = Math.max(left, right);
		int t = Math.min(top, bottom);
		int b = Math.max(top, bottom);
		
		this.padding.setPadding(l, t, r, b);
	}
	
	public GuiPadding getPadding()
	{
		return this.padding;
	}
	
	public void setAnchor(ReadableVector4f vector)
	{
		this.setAnchor(vector.getX(), vector.getY(), vector.getZ(), vector.getW());
	}
	
	public void setAnchor(float minX, float minY, float maxX, float maxY)
	{
		float l = Math.min(minX, maxX);
		float r = Math.max(minX, maxX);
		float t = Math.min(minY, maxY);
		float b = Math.max(minY, maxY);
		
		this.anchor.set(l, t, r, b);
	}
	
	public Vector4f getAnchor()
	{
		return this.anchor;
	}
	
	public void setDrawDepth(int depth)
	{
		this.drawDepth = depth;
	}
	
	@Override
	public void setParent(IGuiRect trans)
	{
		this.parentTranform = trans != null? trans : GuiRectangle.ZERO;
	}
	
	@Override
	public IGuiRect getParent()
	{
		return this.parentTranform;
	}
	
	@Override
	public int getX()
	{
		IGuiRect frame = this.parentTranform;
		return frame.getX() + (int)(frame.getWidth() * this.anchor.x) + padding.getLeft();
	}
	
	@Override
	public int getY()
	{
		IGuiRect frame = this.parentTranform;
		return frame.getY() + (int)(frame.getHeight() * this.anchor.y) + padding.getTop();
	}
	
	@Override
	public int getWidth()
	{
		IGuiRect frame = this.parentTranform;
		return (int)(frame.getWidth() * (this.anchor.z - this.anchor.x)) - (padding.getRight() + padding.getLeft());
	}
	
	@Override
	public int getHeight()
	{
		IGuiRect frame = this.parentTranform;
		return (int)(frame.getHeight() * (this.anchor.w - this.anchor.y)) - (padding.getBottom() + padding.getTop());
	}
	
	@Override
	public int getDepth()
	{
		return this.drawDepth;
	}
	
	@Override
	public boolean contains(int x3, int y3)
	{
		int x = getX();
		int y = getY();
		int w = getWidth();
		int h = getHeight();
		int x1 = x;
		int x2 = x + w;
		int y1 = y;
		int y2 = y + h;
		return x3 >= x1 && x3 < x2 && y3 >= y1 && y3 < y2;
	}

	@Override
	public int compareTo(IGuiRect o)
	{
		return (int)Math.signum(o.getDepth() - drawDepth);
	}
}
