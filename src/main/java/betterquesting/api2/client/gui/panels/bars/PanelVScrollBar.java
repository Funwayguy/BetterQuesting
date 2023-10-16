package betterquesting.api2.client.gui.panels.bars;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.util.List;

public class PanelVScrollBar implements IScrollBar {
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

  public PanelVScrollBar(IGuiRect rect) {
    transform = rect;
    setBarTexture(PresetTexture.SCROLL_V_BG.getTexture(), PresetTexture.SCROLL_V_0.getTexture(),
                  PresetTexture.SCROLL_V_1.getTexture(), PresetTexture.SCROLL_V_2.getTexture());
  }

  @Override
  public PanelVScrollBar setHandleSize(int size, int inset) {
    hSize = size;
    this.inset = inset;
    return this;
  }

  @Override
  public PanelVScrollBar setBarTexture(IGuiTexture back, IGuiTexture handleDisabled, IGuiTexture handleIdle,
                                       IGuiTexture handleHover) {
    texBack = back;
    texHandleState[0] = handleDisabled;
    texHandleState[1] = handleIdle;
    texHandleState[2] = handleHover;
    return this;
  }

  public PanelVScrollBar setScrollSpeed(float f) {
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
      float cy = (float) (my - (bounds.getY() + hSize / 2)) / (float) (bounds.getHeight() - hSize);
      writeValue(cy);
    } else if (isDragging) {
      isDragging = false;
    }

    GlStateManager.pushMatrix();
    GlStateManager.color(1F, 1F, 1F, 1F);

    if (texBack != null) {
      texBack.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
    }

    int sy = MathHelper.floor((bounds.getHeight() - hSize - (inset * 2)) * scroll);
    int state = !active ? 0 : (isDragging || bounds.contains(mx, my) ? 2 : 1);
    IGuiTexture tex = texHandleState[state];

    if (tex != null) {
      tex.drawTexture(bounds.getX() + inset, bounds.getY() + sy + inset, bounds.getWidth() - (inset * 2), hSize, 0F,
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

    float dy = sdx * speed;

    if ((dy < 0F && scroll <= 0F) || (dy > 0F && scroll >= 1F)) {
      return false;
    } else {
      writeValue(dy + scroll);
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
