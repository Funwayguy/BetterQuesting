package betterquesting.api2.client.gui.panels.content;

import static betterquesting.api.storage.BQ_Settings.textWidthCorrection;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class PanelTextBox implements IGuiPanel {
    private final GuiRectText transform;
    private boolean enabled = true;

    private String text = "";
    private boolean shadow = false;
    private IGuiColor color = new GuiColorStatic(255, 255, 255, 255);
    private final boolean autoFit;
    private int align = 0;
    private int fontScale = 12;

    private int lines = 1; // Cached number of lines

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

        IGuiRect bounds = this.getTransform();
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        if (autoFit) {
            List<String> sl = fr.listFormattedStringToWidth(text, bounds.getWidth());
            lines = sl.size() - 1;

            this.transform.h = fr.FONT_HEIGHT * sl.size();
        } else {
            lines = (bounds.getHeight() / fr.FONT_HEIGHT) - 1;
        }

        return this;
    }

    public PanelTextBox setColor(IGuiColor color) {
        this.color = color;
        return this;
    }

    public PanelTextBox setAlignment(int align) {
        this.align = MathHelper.clamp_int(align, 0, 2);
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

    @Override
    public IGuiRect getTransform() {
        return transform;
    }

    @Override
    public void initPanel() {
        IGuiRect bounds = this.getTransform();
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        float scale = fontScale / 12F;

        if (!autoFit) {
            lines = (int) Math.floor(bounds.getHeight() / (fr.FONT_HEIGHT * scale)) - 1;
            return;
        }

        List<String> sl =
                fr.listFormattedStringToWidth(text, (int) Math.floor(bounds.getWidth() / scale / textWidthCorrection));
        lines = sl.size() - 1;

        this.transform.h = (int) Math.floor(fr.FONT_HEIGHT * sl.size() * scale);
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
        IGuiRect bounds = this.getTransform();
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        float s = fontScale / 12F;
        int w = (int) Math.ceil(RenderUtils.getStringWidth(text, fr) * s);
        int bw = (int) Math.floor(bounds.getWidth() / s / textWidthCorrection);

        if (bw <= 0) return;

        GL11.glPushMatrix();
        GL11.glTranslatef(bounds.getX(), bounds.getY(), 1);
        GL11.glScalef(s, s, 1F);

        if (align == 2 && bw >= w) {
            RenderUtils.drawSplitString(fr, text, bw - w, 0, bw, color.getRGB(), shadow, 0, lines);
        } else if (align == 1 && bw >= w) {
            RenderUtils.drawSplitString(fr, text, bw / 2 - w / 2, 0, bw, color.getRGB(), shadow, 0, lines);
        } else {
            RenderUtils.drawSplitString(fr, text, 0, 0, bw, color.getRGB(), shadow, 0, lines);
        }

        GL11.glPopMatrix();
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

        /*@Override
        public void translate(int x, int y)
        {
        	proxy.translate(x, y);
        }*/

        @Override
        public int compareTo(IGuiRect o) {
            return proxy.compareTo(o);
        }
    }
}
