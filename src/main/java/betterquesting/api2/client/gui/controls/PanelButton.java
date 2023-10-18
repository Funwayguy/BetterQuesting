package betterquesting.api2.client.gui.controls;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.function.Consumer;

public class PanelButton implements IPanelButton, IGuiPanel, INBTSaveLoad<NBTTagCompound> {
  private final IGuiRect transform;
  private boolean enabled = true;

  private final IGuiTexture[] texStates = new IGuiTexture[3];
  private final IGuiColor[] colStates = new IGuiColor[] { new GuiColorStatic(128, 128, 128, 255),
                                                          new GuiColorStatic(255, 255, 255, 255),
                                                          new GuiColorStatic(16777120) };
  private IGuiTexture texIcon = null;
  private IGuiColor colIcon = null;
  private int icoPadding = 0;
  private List<String> tooltip = null;
  private boolean txtShadow = true;
  private String btnText;
  private int textAlign = 1;
  private boolean isActive = true;
  private final int btnID;

  private boolean pendingRelease = false;

  private Consumer<PanelButton> clickAction = null;

  public PanelButton(IGuiRect rect, int id, String txt) {
    transform = rect;
    btnText = txt;
    btnID = id;

    setTextures(PresetTexture.BTN_NORMAL_0.getTexture(), PresetTexture.BTN_NORMAL_1.getTexture(),
                PresetTexture.BTN_NORMAL_2.getTexture());
    setTextHighlight(PresetColor.BTN_DISABLED.getColor(), PresetColor.BTN_IDLE.getColor(),
                     PresetColor.BTN_HOVER.getColor());
  }

  public PanelButton setClickAction(Consumer<PanelButton> action) {
    clickAction = action;
    return this;
  }

  public PanelButton setTextHighlight(IGuiColor disabled, IGuiColor idle, IGuiColor hover) {
    colStates[0] = disabled;
    colStates[1] = idle;
    colStates[2] = hover;
    return this;
  }

  public PanelButton setTextShadow(boolean enabled) {
    txtShadow = enabled;
    return this;
  }

  public PanelButton setTextAlignment(int align) {
    textAlign = MathHelper.clamp(align, 0, 2);
    return this;
  }

  public PanelButton setTextures(IGuiTexture disabled, IGuiTexture idle, IGuiTexture hover) {
    texStates[0] = disabled;
    texStates[1] = idle;
    texStates[2] = hover;
    return this;
  }

  public PanelButton setIcon(IGuiTexture icon) {
    return setIcon(icon, 0);
  }

  public PanelButton setIcon(IGuiTexture icon, int padding) {
    return setIcon(icon, null, padding);
  }

  public PanelButton setIcon(IGuiTexture icon, IGuiColor color, int padding) {
    texIcon = icon;
    colIcon = color;
    icoPadding = padding * 2;
    return this;
  }

  public PanelButton setTooltip(List<String> tooltip) {
    this.tooltip = tooltip;
    return this;
  }

  public void setText(String text) {
    btnText = text;
  }

  public String getText() {
    return btnText;
  }

  @Override
  public int getButtonID() {
    return btnID;
  }

  @Override
  public boolean isActive() {
    return isActive;
  }

  @Override
  public void setActive(boolean state) {
    isActive = state;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean state) {
    enabled = state;
  }

  @Override
  public IGuiRect getTransform() {
    return transform;
  }

  @Override
  public void initPanel() { }

  @Override
  public void drawPanel(int mx, int my, float partialTick) {
    IGuiRect bounds = getTransform();
    GlStateManager.pushMatrix();
    GlStateManager.color(1F, 1F, 1F, 1F);
    int curState = !isActive() ? 0 : (bounds.contains(mx, my) ? 2 : 1);

    if (curState == 2 && pendingRelease && Mouse.isButtonDown(0)) {
      curState = 0;
    }

    IGuiTexture t = texStates[curState];

    if (t != null) // Support for text or icon only buttons in one or more states.
    {
      t.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
    }

    if (texIcon != null) {
      int isz = Math.min(bounds.getHeight() - icoPadding, bounds.getWidth() - icoPadding);

      if (isz > 0) {
        if (colIcon != null) {
          texIcon.drawTexture(bounds.getX() + (bounds.getWidth() / 2) - (isz / 2),
                              bounds.getY() + (bounds.getHeight() / 2) - (isz / 2), isz, isz, 0F, partialTick, colIcon);
        } else {
          texIcon.drawTexture(bounds.getX() + (bounds.getWidth() / 2) - (isz / 2),
                              bounds.getY() + (bounds.getHeight() / 2) - (isz / 2), isz, isz, 0F, partialTick);
        }
      }
    }

    if (btnText != null && !btnText.isEmpty()) {
      drawCenteredString(Minecraft.getMinecraft().fontRenderer, btnText, bounds.getX(),
                         bounds.getY() + bounds.getHeight() / 2 - 4, bounds.getWidth(), colStates[curState].getRGB(),
                         txtShadow, textAlign);
    }

    GlStateManager.popMatrix();
  }

  private static void drawCenteredString(FontRenderer font, String text, int x, int y, int width, int color,
                                         boolean shadow, int align) {
    switch (align) {
      case 0: {
        font.drawString(text, x + 4, y, color, shadow);
        break;
      }
      case 2: {
        font.drawString(text, x + width - RenderUtils.getStringWidth(text, font) / 2F - 4, y, color, shadow);
        break;
      }
      default: {
        font.drawString(text, x + Math.floorDiv(width, 2) - RenderUtils.getStringWidth(text, font) / 2F, y, color,
                        shadow);
      }
    }
  }

  @Override
  public boolean onMouseClick(int mx, int my, int click) {
    boolean contains = getTransform().contains(mx, my);
    pendingRelease = isActive() && click == 0 && contains;

    return (click == 0 || click == 1) && contains;
  }

  @Override
  public boolean onMouseRelease(int mx, int my, int click) {
    if (!pendingRelease) {
      return false;
    }

    pendingRelease = false;

    IGuiRect bounds = getTransform();
    boolean clicked = isActive() && click == 0 && bounds.contains(mx, my) &&
                      !PEventBroadcaster.INSTANCE.postEvent(new PEventButton(this));

    if (clicked) {
      Minecraft.getMinecraft().getSoundHandler()
               .playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      onButtonClick();
    }

    return clicked;
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
    if (getTransform().contains(mx, my)) {
      return tooltip;
    }

    return null;
  }

  @Override
  public void onButtonClick() {
    if (clickAction != null) {
      clickAction.accept(this);
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    // TODO: Fix me
    return nbt;
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) { }
}
