package betterquesting.api2.client.gui.misc;

public interface IGuiRect extends Comparable<IGuiRect> {
    int getX();

    int getY();

    int getWidth();

    int getHeight();

    int getDepth();

    IGuiRect getParent();

    void setParent(IGuiRect rect);

    boolean contains(int x, int y);
}
