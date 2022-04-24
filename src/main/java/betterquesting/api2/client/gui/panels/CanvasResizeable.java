package betterquesting.api2.client.gui.panels;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.ComparatorGuiDepth;
import betterquesting.api2.client.gui.misc.GuiRectLerp;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class CanvasResizeable implements IGuiCanvas {
    private final List<IGuiPanel> guiPanels = new CopyOnWriteArrayList<>();

    private IGuiTexture bgTexture;
    private final GuiRectLerp rectLerp;
    private boolean enabled = true;
    private final boolean crop;

    public CanvasResizeable(IGuiRect rect, IGuiTexture texture) {
        this(rect, texture, true);
    }

    public CanvasResizeable(IGuiRect rect, IGuiTexture texture, boolean crop) {
        this.bgTexture = texture;
        this.rectLerp = new GuiRectLerp(rect);
        this.crop = crop;
    }

    public void changeBG(@Nullable IGuiTexture texture) {
        this.bgTexture = texture;
    }

    @Override
    public IGuiRect getTransform() {
        return crop ? rectLerp.getProxyRect() : rectLerp;
    }

    public GuiRectLerp getRectLerp() {
        return this.rectLerp;
    }

    @Nonnull
    @Override
    public List<IGuiPanel> getChildren() {
        return this.guiPanels;
    }

    @Override
    public void initPanel() {
        this.guiPanels.clear();
    }

    @Override
    public void setEnabled(boolean state) {
        this.enabled = state;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void lerpToRect(@Nonnull IGuiRect rect, long time, boolean inheritParent) {
        if (inheritParent) rect.setParent(rectLerp.getParent());
        rectLerp.lerpTo(rect, time);
    }

    public void snapToRect(@Nonnull IGuiRect rect, boolean inheritParent) {
        if (inheritParent) rect.setParent(rectLerp.getParent());
        rectLerp.snapTo(rect);
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        if (crop) RenderUtils.startScissor(rectLerp);

        if (bgTexture != null) {
            IGuiRect bounds = rectLerp;
            GlStateManager.pushMatrix();
            GlStateManager.color(1F, 1F, 1F, 1F);
            bgTexture.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
            GlStateManager.popMatrix();
        }

        for (IGuiPanel entry : guiPanels) {
            if (entry.isEnabled()) entry.drawPanel(mx, my, partialTick);
        }

        if (crop) RenderUtils.endScissor();
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        boolean used = false;

        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (entry.isEnabled() && entry.onMouseClick(mx, my, click)) {
                used = true;
                break;
            }
        }

        return used || (bgTexture != null && rectLerp.contains(mx, my));
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        boolean used = false;

        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (entry.isEnabled() && entry.onMouseRelease(mx, my, click)) {
                used = true;
                break;
            }
        }

        return used || (bgTexture != null && rectLerp.contains(mx, my));
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        boolean used = false;

        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (entry.isEnabled() && entry.onMouseScroll(mx, my, scroll)) {
                used = true;
                break;
            }
        }

        return used || (bgTexture != null && rectLerp.contains(mx, my));
    }

    @Override
    public boolean onKeyTyped(char c, int keycode) {
        boolean used = false;

        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (entry.isEnabled() && entry.onKeyTyped(c, keycode)) {
                used = true;
                break;
            }
        }

        return used;
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());
        List<String> tt;

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (!entry.isEnabled()) {
                continue;
            }

            tt = entry.getTooltip(mx, my);

            if (tt != null) {
                return tt;
            }
        }

        return (bgTexture != null && rectLerp.contains(mx, my)) ? Collections.emptyList() : null;
    }

    @Override
    public void addPanel(IGuiPanel panel) {
        if (panel == null || guiPanels.contains(panel)) {
            return;
        }

        guiPanels.add(panel);
        guiPanels.sort(ComparatorGuiDepth.INSTANCE);
        panel.getTransform().setParent(getTransform());
        panel.initPanel();
    }

    @Override
    public boolean removePanel(IGuiPanel panel) {
        return guiPanels.remove(panel);
    }

    @Override
    public void resetCanvas() {
        guiPanels.clear();
    }
}
