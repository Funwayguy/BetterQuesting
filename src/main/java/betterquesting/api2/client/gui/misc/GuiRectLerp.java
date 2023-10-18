package betterquesting.api2.client.gui.misc;

import betterquesting.api.utils.RenderUtils;

import javax.annotation.Nonnull;

public class GuiRectLerp implements IGuiRect {
  private IGuiRect startRect;
  private IGuiRect targetRect;
  private long duration = 200L;
  private long et;

  private final ProxyRect pxRect;

  public GuiRectLerp(@Nonnull IGuiRect start) {
    startRect = start;
    targetRect = start;
    pxRect = new ProxyRect(start);
    et = System.currentTimeMillis();
  }

  public void lerpTo(@Nonnull IGuiRect target, long time) {
    if (time <= 0) {
      return;
    }

    targetRect = target;
    duration = time;
    et = System.currentTimeMillis();
    pxRect.changeReference(target);
  }

  public void snapTo(@Nonnull IGuiRect target) {
    startRect = target;
    targetRect = target;
    et = System.currentTimeMillis();
    pxRect.changeReference(target);
  }

  // Allows other transforms to snap to new destinations even if the targetRect variable changes instance
  public IGuiRect getProxyRect() {
    return pxRect;
  }

  public boolean isIdle() {
    if (startRect == targetRect) {
      return true;
    }
    if (System.currentTimeMillis() - et >= duration) {
      startRect = targetRect;
      return true;
    }
    return false;
  }

  @Override
  public int getX() {
    return isIdle() ? targetRect.getX() : (int) Math.round(
        RenderUtils.lerpDouble(startRect.getX(), targetRect.getX(),
                               (System.currentTimeMillis() - et) / (double) duration));
  }

  @Override
  public int getY() {
    return isIdle() ? targetRect.getY() : (int) Math.round(
        RenderUtils.lerpDouble(startRect.getY(), targetRect.getY(),
                               (System.currentTimeMillis() - et) / (double) duration));
  }

  @Override
  public int getWidth() {
    return isIdle() ? targetRect.getWidth() : (int) Math.round(
        RenderUtils.lerpDouble(startRect.getWidth(), targetRect.getWidth(),
                               (System.currentTimeMillis() - et) / (double) duration));
  }

  @Override
  public int getHeight() {
    return isIdle() ? targetRect.getHeight() : (int) Math.round(
        RenderUtils.lerpDouble(startRect.getHeight(), targetRect.getHeight(),
                               (System.currentTimeMillis() - et) / (double) duration));
  }

  @Override
  public int getDepth() {
    return targetRect.getDepth();
  }

  @Override
  public IGuiRect getParent() {
    return targetRect.getParent();
  }

  @Override
  public void setParent(IGuiRect rect) {
    targetRect.setParent(rect);
  }

  @Override
  public boolean contains(int x, int y) {
    return targetRect.contains(x, y);
  }

  @Override
  public int compareTo(@Nonnull IGuiRect o) {
    return targetRect.compareTo(o);
  }
}
