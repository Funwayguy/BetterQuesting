package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.controls.io.FloatSimpleIO;
import betterquesting.api2.client.gui.misc.ComparatorGuiDepth;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class CanvasScrolling implements IGuiCanvas {
	private final List<IGuiPanel> guiPanels = new CopyOnWriteArrayList<>();
	private final IGuiRect transform;
	private boolean enabled = true;
	
	// Scrolling bounds
	protected final GuiRectangle scrollBounds = new GuiRectangle(0, 0, 0, 0);
	private final GuiRectangle scrollWindow = new GuiRectangle(0, 0, 0, 0);
	protected boolean extendedScroll = false;
	protected boolean zoomMode = false;
	protected int margin = 0;
	// Scroll and zoom drivers
	protected IValueIO<Float> scrollX;
	protected IValueIO<Float> scrollY;
	protected IValueIO<Float> zoomScale;
	
	private boolean isDragging = false; // Mouse buttons held for dragging
	private boolean hasDragged = false; // Dragging used. Don't fire onMouseRelease
	protected int scrollSpeed = (int) (12 * BQ_Settings.scrollMultiplier);
	
	// Starting drag scroll values
	private float dragSX = 0;
	private float dragSY = 0;
	// Starting drag mouse positions
	private int dragMX = 0;
	private int dragMY = 0;
	// Last known scroll position (unscaled)
    protected float lsz = 1F;
    protected int lsx = 0;
    protected int lsy = 0;

    // Enables the auto-disabling panels outside the cropped region. Useful for very large lists
    private boolean useBlocking = true;
    private final CanvasCullingManager cullingManager = new CanvasCullingManager();
    private final GuiRectangle refRect = new GuiRectangle(0, 0, 0, 0);

    public CanvasScrolling(IGuiRect rect) {
        this.transform = rect;

        // Dummy value drivers
        scrollX = new FloatSimpleIO().setLerp(false, 0.02F);
        scrollY = new FloatSimpleIO().setLerp(false, 0.02F);
        zoomScale = new FloatSimpleIO(1F, 0.2F, 2F).setLerp(BQ_Settings.zoomTimeInMs > 0, BQ_Settings.zoomTimeInMs > 0 ? 1.0F / BQ_Settings.zoomTimeInMs : 0);
    }

    public CanvasScrolling setScrollDriverX(IValueIO<Float> driver) {
        this.scrollX = driver;
        return this;
    }

    public CanvasScrolling setScrollDriverY(IValueIO<Float> driver) {
        this.scrollY = driver;
        return this;
    }

    public CanvasScrolling setZoomDriver(IValueIO<Float> driver) {
        this.zoomScale = driver;
        return this;
    }

    public CanvasScrolling setScrollSpeed(int dx) {
        this.scrollSpeed = dx;
        return this;
    }

    public CanvasScrolling setupAdvanceScroll(boolean scrollToZoom, boolean extendedScroll, int scrollMargin) {
        this.zoomMode = scrollToZoom;
        this.extendedScroll = extendedScroll;
        this.margin = scrollMargin;
        return this;
    }

    public CanvasScrolling enableBlocking(boolean state) {
        this.useBlocking = state;
        return this;
    }

    public IGuiRect getScrollBounds() {
        return this.scrollBounds;
    }

    public int getScrollX() {
        return Math.round(scrollBounds.getX() + scrollBounds.getWidth() * scrollX.readValue());
    }

    public int getScrollY() {
        return Math.round(scrollBounds.getY() + scrollBounds.getHeight() * scrollY.readValue());
    }

    public float getZoom() {
        return zoomScale.readValue();
    }

    public void setScrollX(int sx) {
        if (scrollBounds.getWidth() <= 0) return;
        scrollX.writeValueRaw((sx - scrollBounds.getX()) / (float) scrollBounds.getWidth());
        lsx = this.getScrollX();
    }

    public void setScrollY(int sy) {
        if (scrollBounds.getHeight() <= 0) return;
        scrollY.writeValueRaw((sy - scrollBounds.getY()) / (float) scrollBounds.getHeight());
        lsy = this.getScrollY();
    }

    public void setZoom(float z) {
        zoomScale.writeValueRaw(z);
        lsz = zoomScale.readValue();
        this.refreshScrollBounds();
    }

    @Override
    public void initPanel() {
        this.guiPanels.clear();
        this.cullingManager.reset();
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

    @Nonnull
    @Override
    public List<IGuiPanel> getChildren() {
        return this.guiPanels;
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        if (!isRectEqual(refRect, transform)) refreshScrollBounds();

        float zs = zoomScale.readValue();

        int tx = transform.getX();
        int ty = transform.getY();

        if (isDragging && (Mouse.isButtonDown(0) || Mouse.isButtonDown(2))) // Extra fallback incase something used the usual release event
        {
            int dx = (int) ((dragMX - mx) / zs);
            int dy = (int) ((dragMY - my) / zs);

            if (scrollBounds.getWidth() > 0) {
                float dsx = dx / (float) scrollBounds.getWidth() + dragSX;
                scrollX.writeValue(dsx);

                if (!hasDragged && Math.abs(dragSX - scrollX.readValue()) > 0.05F) {
                    hasDragged = true;
                }
            }

            if (scrollBounds.getHeight() > 0) {
                float dsy = dy / (float) scrollBounds.getHeight() + dragSY;
                scrollY.writeValue(dsy);

                if (!hasDragged && Math.abs(dragSY - scrollY.readValue()) > 0.05F) {
                    hasDragged = true;
                }
            }
        } else if (isDragging || hasDragged) {
            isDragging = false;
            hasDragged = false;
        }

        if (lsz != zs) {
            boolean zoomIn = lsz < zs;
            if ((zoomIn && !BQ_Settings.zoomInToCursor) || (!zoomIn && !BQ_Settings.zoomOutToCursor)) {
                if (lsz == 0)
                    return;

                float change = zs / lsz;

                int csx = getScrollX();
                int csy = getScrollY();
                float swcx = scrollWindow.w / 2F;
                float swcy = scrollWindow.h / 2F;
                swcx -= swcx / change;
                swcy -= swcy / change;

                // NOTE: This runs updatePanelScroll() too. Thus the math above is done first before the scroll bounds are changed
                this.refreshScrollBounds();

                if (scrollBounds.getWidth() > 0)
                    scrollX.writeValue(((csx + swcx) - scrollBounds.getX()) / (float) scrollBounds.getWidth());

                if (scrollBounds.getHeight() > 0)
                    scrollY.writeValue(((csy + swcy) - scrollBounds.getY()) / (float) scrollBounds.getHeight());

            } else {
                int csx = getScrollX();
                int csy = getScrollY();

                float swcx = (mx - tx) / (float) transform.getWidth();
                float swcy = (my - ty) / (float) transform.getHeight();

                float dw = scrollWindow.getWidth();
                float dh = scrollWindow.getHeight();

                // NOTE: This runs updatePanelScroll() too. Thus the math above is done first before the scroll bounds are changed
                this.refreshScrollBounds();

                dw -= scrollWindow.getWidth();
                dh -= scrollWindow.getHeight();

                if (scrollBounds.getWidth() > 0)
                    scrollX.writeValue(((csx + swcx * dw) - scrollBounds.getX()) / (float) scrollBounds.getWidth());

                if (scrollBounds.getHeight() > 0)
                    scrollY.writeValue(((csy + swcy * dh) - scrollBounds.getY()) / (float) scrollBounds.getHeight());

            }
            lsx = getScrollX();
            lsy = getScrollY();
            lsz = zs;

        } else if (lsx != getScrollX() || lsy != getScrollY()) { // We can skip this if the above case ran
            this.updatePanelScroll();
        }

        GlStateManager.pushMatrix();

        RenderUtils.startScissor(transform);

        GlStateManager.translate(tx - lsx * zs, ty - lsy * zs, 0F);
        GlStateManager.scale(zs, zs, zs);

        int smx = (int) ((mx - tx) / zs) + lsx;
        int smy = (int) ((my - ty) / zs) + lsy;

        for (IGuiPanel panel : getVisiblePanels()) {
            if (panel.isEnabled()) {
                panel.drawPanel(smx, smy, partialTick);
            }
        }

        RenderUtils.endScissor();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        if (!transform.contains(mx, my)) {
            return false;
        }

        float zs = zoomScale.readValue();
        int tx = transform.getX();
        int ty = transform.getY();
        int smx = (int) ((mx - tx) / zs) + lsx;
        int smy = (int) ((my - ty) / zs) + lsy;

        boolean used = false;

        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (entry.isEnabled() && entry.onMouseClick(smx, smy, click)) {
                used = true;
                break;
            }
        }

        if (!used && (click == 0 || click == 2)) {
            dragSX = scrollX.readValue();
            dragSY = scrollY.readValue();
            dragMX = mx;
            dragMY = my;
            isDragging = true;
            return true;
        }

        return used;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        boolean used = false;

        if (!hasDragged) {
            if (!transform.contains(mx, my)) return false;

            float zs = zoomScale.readValue();
            int tx = transform.getX();
            int ty = transform.getY();
            int smx = (int) ((mx - tx) / zs) + lsx;
            int smy = (int) ((my - ty) / zs) + lsy;

            ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());

            while (pnIter.hasPrevious()) {
                if (pnIter.previous().onMouseRelease(smx, smy, click)) {
                    used = true;
                    break;
                }
            }
        }

        if (isDragging) {
            if (!Mouse.isButtonDown(0) && !Mouse.isButtonDown(2)) isDragging = false;
            return true;
        }

        return used;
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        if (scroll == 0 || !transform.contains(mx, my)) return false;

        float zs = zoomScale.readValue();
        int tx = transform.getX();
        int ty = transform.getY();
        int smx = (int) ((mx - tx) / zs) + lsx;
        int smy = (int) ((my - ty) / zs) + lsy;

        boolean used = false;

        ListIterator<IGuiPanel> pnIter = guiPanels.listIterator(guiPanels.size());

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (entry.isEnabled() && entry.onMouseScroll(smx, smy, scroll)) {
                used = true;
                break;
            }
        }

        if (!used) {
            if (zoomMode) {
                float dy = -scroll * 0.05F;
                float cs = zoomScale.readValueRaw();
                float zoomSpeed = BQ_Settings.zoomSpeed;

                if (scroll > 0) {
                    zoomScale.writeValue(cs / zoomSpeed);
                } else {
                    zoomScale.writeValue(cs * zoomSpeed);
                }

                used = true;
            } else if (scrollBounds.getHeight() > 0) // V scroll
            {
                float dy = (scroll * scrollSpeed) / (float) scrollBounds.getHeight();
                float cs = scrollY.readValue();

                if (!((dy < 0F && cs <= 0F) || (dy > 0F && cs >= 1F))) {
                    scrollY.writeValue(cs + dy);
                    this.updatePanelScroll();
                    used = true;
                }
            } else if (scrollBounds.getWidth() > 0) // H scroll
            {
                float dy = (scroll * scrollSpeed) / (float) scrollBounds.getWidth();
                float cs = scrollX.readValue();

                if (!((dy < 0F && cs <= 0F) || (dy > 0F && cs >= 1F))) {
                    scrollX.writeValue(cs + dy);
                    this.updatePanelScroll();
                    used = true;
                }
            }
        }

        return used;
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
		
		/*if(!used && c == 'c')
        {
            setScrollX(0);
            setScrollY(0);
        }*/

        return used;
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        if (!transform.contains(mx, my) || isDragging) return null;

        float zs = zoomScale.readValue();
        int tx = transform.getX();
        int ty = transform.getY();
        int smx = (int) ((mx - tx) / zs) + lsx;
        int smy = (int) ((my - ty) / zs) + lsy;

        List<IGuiPanel> tmpList = getVisiblePanels();
        ListIterator<IGuiPanel> pnIter = tmpList.listIterator(tmpList.size());
        List<String> tt;

        while (pnIter.hasPrevious()) {
            IGuiPanel entry = pnIter.previous();

            if (!entry.isEnabled()) {
                continue;
            }

            tt = entry.getTooltip(smx, smy);

            if (tt != null && tt.size() > 0) {
                return tt;
            }
        }

        return null;
    }

    @Override
    public void addPanel(IGuiPanel panel) {
        addCulledPanel(panel, true);
    }

    public void addCulledPanel(IGuiPanel panel, boolean useCulling) {
        if (panel == null || guiPanels.contains(panel)) return;

        guiPanels.add(panel);
        guiPanels.sort(ComparatorGuiDepth.INSTANCE);

        cullingManager.addPanel(panel, useCulling);

        panel.initPanel();

        this.refreshScrollBounds();
    }

    @Override
    public boolean removePanel(IGuiPanel panel) {
        boolean b = guiPanels.remove(panel);

        if (b) {
            cullingManager.removePanel(panel);
            this.refreshScrollBounds();
        }

        return b;
    }

    public void refreshScrollBounds() {
        boolean first = true;
        int left = 0;
        int right = 0;
        int top = 0;
        int bottom = 0;

        float zs = zoomScale.readValue();

        for (IGuiPanel panel : guiPanels) {
            if (first) {
                left = panel.getTransform().getX();
                top = panel.getTransform().getY();
                right = panel.getTransform().getX() + panel.getTransform().getWidth();
                bottom = panel.getTransform().getY() + panel.getTransform().getHeight();
                first = false;
            } else {
                left = Math.min(left, panel.getTransform().getX());
                top = Math.min(top, panel.getTransform().getY());
                right = Math.max(right, panel.getTransform().getX() + panel.getTransform().getWidth());
                bottom = Math.max(bottom, panel.getTransform().getY() + panel.getTransform().getHeight());
            }
        }

        left -= margin;
        right += margin;
        top -= margin;
        bottom += margin;

        right -= (int) Math.ceil(this.getTransform().getWidth() / zs);
        bottom -= (int) Math.ceil(this.getTransform().getHeight() / zs);

        if (extendedScroll) {
            scrollBounds.x = Math.min(left, right);
            scrollBounds.y = Math.min(top, bottom);
            scrollBounds.w = Math.max(left, right) - scrollBounds.x;
            scrollBounds.h = Math.max(top, bottom) - scrollBounds.y;
        } else {
            scrollBounds.x = left;
            scrollBounds.y = top;
            scrollBounds.w = Math.max(0, right - left);
            scrollBounds.h = Math.max(0, bottom - top);
        }

        updatePanelScroll();

        refRect.x = transform.getX();
        refRect.y = transform.getY();
        refRect.w = transform.getWidth();
        refRect.h = transform.getHeight();
    }

    public void updatePanelScroll() {
        lsx = this.getScrollX();
        lsy = this.getScrollY();

        float zs = zoomScale.readValue();

        scrollWindow.x = lsx;
        scrollWindow.y = lsy;
        scrollWindow.w = (int) Math.ceil(transform.getWidth() / zs);
        scrollWindow.h = (int) Math.ceil(transform.getHeight() / zs);

        cullingManager.updateVisiblePanels(scrollWindow);
    }

    @Override
    public void resetCanvas() {
        guiPanels.clear();
        cullingManager.reset();
        refreshScrollBounds();
    }

    private List<IGuiPanel> getVisiblePanels() {
        return useBlocking ? cullingManager.getVisiblePanels() : guiPanels;
    }

    private boolean isRectEqual(IGuiRect r1, IGuiRect r2) {
        return r1.getX() == r2.getX() && r1.getY() == r2.getY() && r1.getWidth() == r2.getWidth() && r1.getHeight() == r2.getHeight();
    }
}
