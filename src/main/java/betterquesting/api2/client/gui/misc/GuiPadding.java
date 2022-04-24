package betterquesting.api2.client.gui.misc;

public class GuiPadding {
    public int l, t, r, b = 0;

    public GuiPadding() {
        this(0, 0, 0, 0);
    }

    public GuiPadding(int left, int top, int right, int bottom) {
        this.setPadding(left, top, right, bottom);
    }

    public GuiPadding copy() {
        return new GuiPadding(l, t, r, b);
    }

    public GuiPadding setPadding(int left, int top, int right, int bottom) {
        this.l = left;
        this.t = top;

        this.r = right;
        this.b = bottom;

        return this;
    }

    public int getLeft() {
        return l;
    }

    public int getTop() {
        return t;
    }

    public int getRight() {
        return r;
    }

    public int getBottom() {
        return b;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[left=" + l + ",top=" + t + ",right=" + r + ",bottom=" + b + "]";
    }
}
