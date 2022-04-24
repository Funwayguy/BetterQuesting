package betterquesting.api2.client.gui.panels.content;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

/**
 * Sets up a panel with texture and tooltip options. Useful for simple images or even labelling an invisible region with a tooltip
 */
public class PanelGeneric implements IGuiPanel {
    private final IGuiRect transform;
    private boolean enabled = true;

    private IGuiTexture texture;
    private IGuiColor color;

    private List<String> tooltip = null;

    public PanelGeneric(IGuiRect rect, IGuiTexture texture) {
        this(rect, texture, null);
    }

    public PanelGeneric(IGuiRect rect, IGuiTexture texture, IGuiColor color) {
        this.transform = rect;
        this.texture = texture;
        this.color = color;
    }

    public void setTooltip(List<String> tooltip) {
        this.tooltip = tooltip;
    }

    public void setTexture(IGuiTexture texture, IGuiColor color) {
        this.texture = texture;
        this.color = color;
    }

    @Override
    public IGuiRect getTransform() {
        return transform;
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
    public void drawPanel(int mx, int my, float partialTick) {
        if (texture != null) {
            GlStateManager.pushMatrix();

            if (color != null) {
                texture.drawTexture(transform.getX(), transform.getY(), transform.getWidth(), transform.getHeight(), 0F, partialTick, color);
            } else {
                texture.drawTexture(transform.getX(), transform.getY(), transform.getWidth(), transform.getHeight(), 0F, partialTick);
            }

            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean onMouseClick(int mx, int my, int button) {
        return false;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int button) {
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
        if (transform.contains(mx, my)) {
            return tooltip;
        }

        return null;
    }
}
