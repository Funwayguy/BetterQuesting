package betterquesting.api2.client.gui.misc;

import org.lwjgl.util.vector.ReadableVector4f;
import org.lwjgl.util.vector.Vector4f;

public final class GuiTransform implements IGuiRect {
    private IGuiRect parent;
    private final Vector4f anchor; // TODO: Change to one that accounts for min-max dimensions
    private final GuiPadding padding;
    private int drawOrder;

    public GuiTransform() {
        this(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0);
    }

    public GuiTransform(ReadableVector4f anchor) {
        this(anchor, new GuiPadding(0, 0, 0, 0), 0);
    }

    public GuiTransform(ReadableVector4f anchor, int xOff, int yOff, int width, int height, int order) {
        this(new Vector4f(anchor.getX(), anchor.getY(), anchor.getX(), anchor.getY()), new GuiPadding(xOff, yOff, -xOff - width, -yOff - height), order);
    }

    public GuiTransform(ReadableVector4f anchor, GuiPadding padding, int depth) {
        this(new Vector4f(anchor), padding, depth);
    }

    public GuiTransform(Vector4f anchor, GuiPadding padding, int depth) {
        this.anchor = anchor;
        this.padding = padding;
        this.drawOrder = depth;

        float l = Math.min(anchor.x, anchor.z);
        float r = Math.max(anchor.x, anchor.z);
        float t = Math.min(anchor.y, anchor.w);
        float b = Math.max(anchor.y, anchor.w);

        this.anchor.x = l;
        this.anchor.y = t;
        this.anchor.z = r;
        this.anchor.w = b;
    }

    public GuiTransform copy() {
        GuiTransform trans = new GuiTransform(new Vector4f(anchor), padding.copy(), drawOrder);
        trans.setParent(this.parent);
        return trans;
    }

    public GuiPadding getPadding() {
        return this.padding;
    }

    public Vector4f getAnchor() {
        return this.anchor;
    }

    public void setDrawDepth(int order) {
        this.drawOrder = order;
    }

    @Override
    public int getX() {
        int i = parent == null ? 0 : (parent.getX() + (int) Math.ceil(parent.getWidth() * this.anchor.x));
        return i + padding.getLeft();
    }

    @Override
    public int getY() {
        int i = parent == null ? 0 : (parent.getY() + (int) Math.ceil(parent.getHeight() * this.anchor.y));
        return i + padding.getTop();
    }

    @Override
    public int getWidth() {
        int i = parent == null ? 0 : (int) Math.ceil(parent.getWidth() * (this.anchor.z - this.anchor.x));
        return i - (padding.getRight() + padding.getLeft());
    }

    @Override
    public int getHeight() {
        int i = parent == null ? 0 : (int) Math.ceil(parent.getHeight() * (this.anchor.w - this.anchor.y));
        return i - (padding.getBottom() + padding.getTop());
    }

    @Override
    public int getDepth() {
        return this.drawOrder;
    }

    @Override
    public IGuiRect getParent() {
        return parent;
    }

    @Override
    public void setParent(IGuiRect rect) {
        this.parent = rect;
    }

    @Override
    public boolean contains(int x3, int y3) {
        int x1 = getX();
        int y1 = getY();
        int w = getWidth();
        int h = getHeight();
        int x2 = x1 + w;
        int y2 = y1 + h;
        return x3 >= x1 && x3 < x2 && y3 >= y1 && y3 < y2;
    }
	
	/*@Override
	public void translate(int x, int y)
	{
		this.padding.setPadding(padding.getLeft() + x, padding.getTop() + y, padding.getRight() - x, padding.getBottom() - y);
	}*/

    @Override
    public int compareTo(IGuiRect o) {
        return (int) Math.signum(o.getDepth() - drawOrder);
    }
}
