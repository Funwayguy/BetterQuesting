package betterquesting.api2.client.gui.misc;

import javax.annotation.Nonnull;

// Used as a means to hotswap a rect without disturbing the parenting heirachy
public class ProxyRect implements IGuiRect {
    private IGuiRect ref;

    public ProxyRect(IGuiRect refRect) {
        this.ref = refRect;
    }

    public void changeReference(IGuiRect refRect) {
        this.ref = refRect;
    }

    @Override
    public int getX() {
        return ref != null ? ref.getX() : 0;
    }

    @Override
    public int getY() {
        return ref != null ? ref.getY() : 0;
    }

    @Override
    public int getWidth() {
        return ref != null ? ref.getWidth() : 0;
    }

    @Override
    public int getHeight() {
        return ref != null ? ref.getHeight() : 0;
    }

    @Override
    public int getDepth() {
        return ref != null ? ref.getDepth() : 0;
    }

    @Override
    public IGuiRect getParent() {
        return ref != null ? ref.getParent() : null;
    }

    @Override
    public void setParent(IGuiRect rect) {
        if (ref != null) ref.setParent(rect);
    }

    @Override
    public boolean contains(int x, int y) {
        return ref != null && ref.contains(x, y);
    }

    @Override
    public int compareTo(@Nonnull IGuiRect o) {
        return ref != null ? ref.compareTo(o) : 0;
    }
}
