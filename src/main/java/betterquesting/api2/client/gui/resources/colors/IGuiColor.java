package betterquesting.api2.client.gui.resources.colors;

public interface IGuiColor {
    int getRGB();

    float getRed();

    float getGreen();

    float getBlue();

    float getAlpha();

    void applyGlColor();
}
