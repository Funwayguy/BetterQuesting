package betterquesting.api2.client.gui.panels.content;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class PanelTextBox implements IGuiPanel {
    private final GuiRectText transform;
    private final GuiRectangle refRect = new GuiRectangle(0, 0, 0, 0, 0);
    private boolean enabled = true;

    private String text = "";
    private boolean shadow = false;
    private IGuiColor color = new GuiColorStatic(255, 255, 255, 255);
    private final boolean autoFit;
    private int align = 0;
    private int fontScale = 12;

    private int lines = 1; // Cached number of lines
    private final float relScale = 12F;

    public PanelTextBox(IGuiRect rect, String text) {
        this(rect, text, false);
    }

    public PanelTextBox(IGuiRect rect, String text, boolean autoFit) {
        this.transform = new GuiRectText(rect, autoFit);
        this.setText(text);
        this.autoFit = autoFit;
    }

    public PanelTextBox setText(String text) {
        this.text = text;

        refreshText();

        return this;
    }

    public PanelTextBox setColor(IGuiColor color) {
        this.color = color;
        return this;
    }

    public PanelTextBox setAlignment(int align) {
        this.align = MathHelper.clamp(align, 0, 2);
        return this;
    }

    public PanelTextBox setFontSize(int size) {
        this.fontScale = size;
        return this;
    }

    public PanelTextBox enableShadow(boolean enable) {
        this.shadow = enable;
        return this;
    }

    private void refreshText() {
        IGuiRect bounds = this.getTransform();
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        float scale = fontScale / relScale;

        if (!autoFit) {
            lines = (int) Math.floor(bounds.getHeight() / (fr.FONT_HEIGHT * scale)) - 1;
            return;
        }

        int w = (int) Math.floor(bounds.getWidth() / scale);
        List<String> sl = w > 8 ? fr.listFormattedStringToWidth(text, w) : Collections.emptyList();
        lines = sl.size() - 1;

        this.transform.h = (int) Math.floor(fr.FONT_HEIGHT * sl.size() * scale);

        refRect.x = bounds.getX();
        refRect.y = bounds.getY();
        refRect.w = bounds.getWidth();
        refRect.h = bounds.getHeight();
    }

    @Override
    public IGuiRect getTransform() {
        return transform;
    }

    @Override
    public void initPanel() {
        refreshText();
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
        if (!isRectEqual(refRect, getTransform()))
            refreshText(); // Makes this panel work with resizable canvases without having to update every frame
        if (lines < 0) return;

        IGuiRect bounds = this.getTransform();
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        //FontRenderer fr = BqFontRenderer.FONT_UNICODE;
        //FontRenderer fr = BqFontRenderer.FONT_STANDARD;

        double s = fontScale / relScale;
        int bw = (int) Math.ceil(bounds.getWidth() / s);
        double w = Math.min(bw, RenderUtils.getStringWidth(text, fr) * s);

        if (bw <= 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(bounds.getX(), bounds.getY(), 1);
        if (align == 1) GlStateManager.translate(bounds.getWidth() / 2D - w / 2D, 0D, 0D);
        if (align == 2) GlStateManager.translate(bounds.getWidth() - w, 0D, 0D);
        GlStateManager.scale(s, s, 1D);

        if (align == 2) {
            RenderUtils.drawSplitString(fr, text, 0, 0, bw, color.getRGB(), shadow, 0, lines);
        } else if (align == 1) {
            RenderUtils.drawSplitString(fr, text, 0, 0, bw, color.getRGB(), shadow, 0, lines);
        } else {
            RenderUtils.drawSplitString(fr, text, 0, 0, bw, color.getRGB(), shadow, 0, lines);
        }

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

    private static class GuiRectText implements IGuiRect {
        private final IGuiRect proxy;
        private final boolean useH;
        private int h;

        public GuiRectText(IGuiRect proxy, boolean useH) {
            this.proxy = proxy;
            this.useH = useH;
        }

        @Override
        public int getX() {
            return proxy.getX();
        }

        @Override
        public int getY() {
            return proxy.getY();
        }

        @Override
        public int getWidth() {
            return proxy.getWidth();
        }

        @Override
        public int getHeight() {
            return useH ? h : proxy.getHeight();
        }

        @Override
        public int getDepth() {
            return proxy.getDepth();
        }

        @Override
        public IGuiRect getParent() {
            return proxy.getParent();
        }

        @Override
        public void setParent(IGuiRect rect) {
            proxy.setParent(rect);
        }

        @Override
        public boolean contains(int x, int y) {
            int x1 = this.getX();
            int x2 = x1 + this.getWidth();
            int y1 = this.getY();
            int y2 = y1 + this.getHeight();
            return x >= x1 && x < x2 && y >= y1 && y < y2;
        }

        @Override
        public int compareTo(@Nonnull IGuiRect o) {
            return proxy.compareTo(o);
        }
    }

    private boolean isRectEqual(IGuiRect r1, IGuiRect r2) {
        return r1.getX() == r2.getX() && r1.getY() == r2.getY() && r1.getWidth() == r2.getWidth() && r1.getHeight() == r2.getHeight();
    }
}
