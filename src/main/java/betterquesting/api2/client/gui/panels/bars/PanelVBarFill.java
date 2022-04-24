package betterquesting.api2.client.gui.panels.bars;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class PanelVBarFill implements IBarFill {
    private final IGuiRect transform;
    private boolean enabled = true;

    private IGuiTexture texBack;
    private IGuiTexture texFill;
    private IValueIO<Float> fillDriver;
    private boolean flipBar = false;
    private IGuiColor color;

    public PanelVBarFill(IGuiRect rect) {
        this.texBack = PresetTexture.METER_V_0.getTexture();
        this.texFill = PresetTexture.METER_V_1.getTexture();

        this.transform = rect;
    }

    @Override
    public PanelVBarFill setFillDriver(IValueIO<Float> driver) {
        this.fillDriver = driver;
        return this;
    }

    @Override
    public PanelVBarFill setFlipped(boolean flipped) {
        this.flipBar = flipped;
        return this;
    }

    @Override
    public PanelVBarFill setFillColor(IGuiColor color) {
        this.color = color;
        return this;
    }

    @Override
    public PanelVBarFill setBarTexture(IGuiTexture back, IGuiTexture front) {
        this.texBack = back;
        this.texFill = front;
        return this;
    }

    @Override
    public void initPanel() {
    }

    @Override
    public void setEnabled(boolean state) {
        this.enabled = state;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public IGuiRect getTransform() {
        return transform;
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        IGuiRect bounds = this.getTransform();
        GlStateManager.pushMatrix();

        GlStateManager.color(1F, 1F, 1F, 1F);

        if (texBack != null) {
            texBack.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
        }

        float f = MathHelper.clamp(fillDriver.readValue(), 0F, 1F);

        if (this.flipBar) {
            RenderUtils.startScissor(new GuiRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), (int) (bounds.getHeight() * f), 0));
        } else {
            RenderUtils.startScissor(new GuiRectangle(bounds.getX(), bounds.getY() + (int) (bounds.getHeight() - (bounds.getHeight() * f)), bounds.getWidth(), (int) (bounds.getHeight() * f), 0));
        }

        if (texFill != null) {
            texFill.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick, color);
        }

        RenderUtils.endScissor();

        GlStateManager.popMatrix();
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        return false;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        return false;
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        return false;
    }

    @Override
    public boolean onKeyTyped(char c, int keycode) {
        return false;
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        return null;
    }
}
