package betterquesting.api2.client.gui.misc;

import javax.annotation.Nonnull;

public class GuiRectangle implements IGuiRect {
    public int x, y, w, h, d;
    private IGuiRect parent = null;

    public GuiRectangle(int x, int y, int w, int h) {
        this(x, y, w, h, 0);
    }

    public GuiRectangle(int x, int y, int w, int h, int d) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.d = d;
    }

    // Mainly to convert a GuiTransform to a fixed size version
    public GuiRectangle(IGuiRect rect) {
        this(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), rect.getDepth());
    }

    @Override
    public int getX() {
        return x + (parent == null ? 0 : parent.getX());
    }

    @Override
    public int getY() {
        return y + (parent == null ? 0 : parent.getY());
    }

    @Override
    public int getWidth() {
        return w;
    }

    @Override
    public int getHeight() {
        return h;
    }

    @Override
    public int getDepth() {
        return d;
    }

    @Override
    public IGuiRect getParent() {
        return this.parent;
    }

    @Override
    public void setParent(IGuiRect rect) {
        this.parent = rect;
    }

    @Override
    public boolean contains(int x, int y) {
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int y1 = this.getY();
        int y2 = y1 + this.getHeight();
        return x >= x1 && x < x2 && y >= y1 && y < y2;
    }
	
	/*@Override
	public void translate(int dx, int dy)
	{
		this.x += dx;
		this.y += dy;
	}*/

    @Override
    public int compareTo(@Nonnull IGuiRect o) {
        return (int) Math.signum(o.getDepth() - d);
    }
}
