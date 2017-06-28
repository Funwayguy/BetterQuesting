package adv_director.rw2.api.client.gui.misc;

import org.lwjgl.util.Rectangle;
import org.lwjgl.util.vector.ReadableVector4f;
import org.lwjgl.util.vector.Vector4f;

public final class GuiTransform implements Comparable<GuiTransform>
{
	private final Vector4f anchor = new Vector4f(0F, 0F, 1F, 1F);
	private final GuiPadding padding;
	private int drawDepth = 0;
	
	public GuiTransform()
	{
		this(new Vector4f(0F, 0F, 1F, 1F), new GuiPadding(0, 0, 1, 1), 0);
	}
	
	public GuiTransform(ReadableVector4f anchor, GuiPadding padding, int depth)
	{
		this.setAnchor(anchor);
		this.padding = padding;
		this.drawDepth = depth;
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
	
	public int getDrawDepth()
	{
		return this.drawDepth;
	}
	
	public Rectangle applyTransform(Rectangle frame)
	{
		int l = frame.getX() + (int)(frame.getWidth() * this.anchor.x) + padding.getLeft();
		int t = frame.getY() + (int)(frame.getHeight() * this.anchor.y) + padding.getTop();
		int w = (int)(frame.getWidth() * (this.anchor.z - this.anchor.x)) - (padding.getRight() + padding.getLeft());
		int h = (int)(frame.getHeight() * (this.anchor.w - this.anchor.y)) - (padding.getBottom() + padding.getTop());
		
		return new Rectangle(l, t, w, h);
	}

	@Override
	public int compareTo(GuiTransform trans)
	{
		return trans.getDrawDepth() - this.getDrawDepth();
	}
}
