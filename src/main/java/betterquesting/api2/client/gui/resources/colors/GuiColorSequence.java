package betterquesting.api2.client.gui.resources.colors;

public class GuiColorSequence implements IGuiColor {
    private final IGuiColor[] colors;
    private final float interval;

    public GuiColorSequence(float interval, IGuiColor... colors) {
        this.colors = colors;
        this.interval = interval;
    }

    @Override
    public int getRGB() {
        return getCurrentColor().getRGB();
    }

    @Override
    public float getRed() {
        return getCurrentColor().getRed();
    }

    @Override
    public float getGreen() {
        return getCurrentColor().getGreen();
    }

    @Override
    public float getBlue() {
        return getCurrentColor().getBlue();
    }

    @Override
    public float getAlpha() {
        return getCurrentColor().getAlpha();
    }

    @Override
    public void applyGlColor() {
        getCurrentColor().applyGlColor();
    }

    public IGuiColor getCurrentColor() {
        if (colors.length <= 0) return null;
        return colors[(int) Math.floor((System.currentTimeMillis() / 1000D) % (colors.length * interval) / interval)];
    }

    public IGuiColor[] getAllColors() {
        return this.colors;
    }
}
