package betterquesting.api2.client.gui.panels.bars;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class PanelHBarFill implements IBarFill {
  private final IGuiRect transform;
  private boolean enabled = true;

  private IGuiTexture texBack;
  private IGuiTexture texFill;
  private IValueIO<Float> fillDriver;
  private boolean flipBar = false;
  private IGuiColor color = new GuiColorStatic(0xFFFFFFFF);

  public PanelHBarFill(IGuiRect rect) {
    texBack = PresetTexture.METER_H_0.getTexture();
    texFill = PresetTexture.METER_H_1.getTexture();

    transform = rect;
  }

  @Override
  public PanelHBarFill setFillDriver(IValueIO<Float> driver) {
    fillDriver = driver;
    return this;
  }

  @Override
  public PanelHBarFill setFlipped(boolean flipped) {
    flipBar = flipped;
    return this;
  }

  @Override
  public PanelHBarFill setFillColor(IGuiColor color) {
    this.color = color;
    return this;
  }

  @Override
  public PanelHBarFill setBarTexture(IGuiTexture back, IGuiTexture front) {
    texBack = back;
    texFill = front;
    return this;
  }

  @Override
  public void initPanel() { }

  @Override
  public void setEnabled(boolean state) {
    enabled = state;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public IGuiRect getTransform() {
    return transform;
  }

  @Override
  public void drawPanel(int mx, int my, float partialTick) {
    IGuiRect bounds = getTransform();
    GlStateManager.pushMatrix();

    GlStateManager.color(1F, 1F, 1F, 1F);

    if (texBack != null) {
      texBack.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
    }

    float f = MathHelper.clamp(fillDriver.readValue(), 0F, 1F);

    if (flipBar) {
      RenderUtils.startScissor(
          new GuiRectangle(bounds.getX() + (int) (bounds.getWidth() - (bounds.getWidth() * f)), bounds.getY(),
                           (int) (bounds.getWidth() * f), bounds.getHeight(), 0));
    } else {
      RenderUtils.startScissor(
          new GuiRectangle(bounds.getX(), bounds.getY(), (int) (bounds.getWidth() * f), bounds.getHeight(), 0));
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
