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
    this.transform = rect;
    this.setBarTexture(PresetTexture.SCROLL_V_BG.getTexture(), PresetTexture.SCROLL_V_0.getTexture(),
                       PresetTexture.SCROLL_V_1.getTexture(), PresetTexture.SCROLL_V_2.getTexture());
  }

  @Override
  public PanelVScrollBar setHandleSize(int size, int inset) {
    this.hSize = size;
    this.inset = inset;
    return this;
  }

  @Override
  public PanelVScrollBar setBarTexture(IGuiTexture back, IGuiTexture handleDisabled, IGuiTexture handleIdle,
                                       IGuiTexture handleHover) {
    this.texBack = back;
    this.texHandleState[0] = handleDisabled;
    this.texHandleState[1] = handleIdle;
    this.texHandleState[2] = handleHover;
    return this;
  }

  public PanelVScrollBar setScrollSpeed(float f) {
    this.speed = f;
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
  public void setActive(boolean state) {
    this.active = state;
  }

  @Override
  public boolean isActive() {
    return this.active;
  }

  @Override
  public IGuiRect getTransform() {
    return transform;
  }

  @Override
  public void drawPanel(int mx, int my, float partialTick) {
    IGuiRect bounds = this.getTransform();

    if (active && isDragging && (Mouse.isButtonDown(0) || Mouse.isButtonDown(2))) {
      float cy = (float) (my - (bounds.getY() + hSize / 2)) / (float) (bounds.getHeight() - hSize);
      this.writeValue(cy);
    } else if (isDragging) {
      this.isDragging = false;
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
    IGuiRect bounds = this.getTransform();
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
    IGuiRect bounds = this.getTransform();
    if (!active || sdx == 0 || !bounds.contains(mx, my)) {
      return false;
    }

    float dy = sdx * speed;

    if ((dy < 0F && scroll <= 0F) || (dy > 0F && scroll >= 1F)) {
      return false;
    } else {
      this.writeValue(dy + scroll);
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
    return this.scroll;
  }

  @Override
  public void writeValue(Float value) {
    this.scroll = MathHelper.clamp(value, 0F, 1F);
  }

  @Override
  public Float readValueRaw() {
    return readValue();
  }

  @Override
  public void writeValueRaw(Float value) {
    this.scroll = value;
  }
}
