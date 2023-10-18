package betterquesting.api2.client.gui.panels.bars;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.util.List;

public class PanelHScrollBar implements IScrollBar {
  private final IGuiRect transform;
  private boolean enabled = true;
  private boolean active = true;

  private IGuiTexture texBack;
  private final IGuiTexture[] texHandleState = new IGuiTexture[3];

  private float scroll = 0F;
  private float speed = 0.1F;
  private int hSize = 16;
  private int inset = 0;
  private boolean isDragging = false;

  public PanelHScrollBar(IGuiRect rect) {
    transform = rect;
    setBarTexture(PresetTexture.SCROLL_H_BG.getTexture(), PresetTexture.SCROLL_H_0.getTexture(),
                  PresetTexture.SCROLL_H_1.getTexture(), PresetTexture.SCROLL_H_2.getTexture());
  }

  @Override
  public PanelHScrollBar setHandleSize(int size, int inset) {
    hSize = size;
    this.inset = inset;
    return this;
  }

  @Override
  public PanelHScrollBar setBarTexture(IGuiTexture back, IGuiTexture handleDisabled, IGuiTexture handleIdle,
                                       IGuiTexture handleHover) {
    texBack = back;
    texHandleState[0] = handleDisabled;
    texHandleState[1] = handleIdle;
    texHandleState[2] = handleHover;
    return this;
  }

  @Override
  public PanelHScrollBar setScrollSpeed(float f) {
    speed = f;
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
  public void setActive(boolean state) {
    active = state;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public IGuiRect getTransform() {
    return transform;
  }

  @Override
  public void drawPanel(int mx, int my, float partialTick) {
    IGuiRect bounds = getTransform();

    if (active && isDragging && (Mouse.isButtonDown(0) || Mouse.isButtonDown(2))) {
      float cx = (float) (mx - (bounds.getX() + hSize / 2)) / (float) (bounds.getWidth() - hSize);
      writeValue(cx);
    } else if (isDragging) {
      isDragging = false;
    }

    GlStateManager.pushMatrix();
    GlStateManager.color(1F, 1F, 1F, 1F);

    if (texBack != null) {
      texBack.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
    }

    int sx = MathHelper.floor((bounds.getWidth() - hSize - (inset * 2)) * scroll);
    int state = !active ? 0 : (isDragging || bounds.contains(mx, my) ? 2 : 1);
    IGuiTexture tex = texHandleState[state];

    if (tex != null) {
      tex.drawTexture(bounds.getX() + sx + inset, bounds.getY() + inset, hSize, bounds.getHeight() - (inset * 2), 0F,
                      partialTick);
    }

    GlStateManager.popMatrix();
  }

  @Override
  public boolean onMouseClick(int mx, int my, int click) {
    IGuiRect bounds = getTransform();

    if (!active || !bounds.contains(mx, my)) {
      return false;
    }

    if (click == 0 || click == 2) {
      isDragging = true;
      return true;
    }

    return false;
  }

  @Override
  public boolean onMouseRelease(int mx, int my, int click) {
    return false;
  }

  @Override
  public boolean onMouseScroll(int mx, int my, int sdx) {
    IGuiRect bounds = getTransform();
    if (!active || sdx == 0 || !bounds.contains(mx, my)) {
      return false;
    }

    float dx = sdx * speed;

    if ((dx < 0 && scroll <= 0F) || (dx > 0 && scroll >= 1)) {
      return false;
    } else {
      writeValue(dx + scroll);
      return true;
    }
  }

  @Override
  public boolean onKeyTyped(char c, int keycode) {
    return false;
  }

  @Override
  public List<String> getTooltip(int mx, int my) {
    return null;
  }

  @Override
  public Float readValue() {
    return scroll;
  }

  @Override
  public void writeValue(Float value) {
    scroll = MathHelper.clamp(value, 0F, 1F);
  }

  @Override
  public Float readValueRaw() {
    return readValue();
  }

  @Override
  public void writeValueRaw(Float value) {
    scroll = value;
  }
}
