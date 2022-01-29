package betterquesting.api2.client.gui.controls;

import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.io.FloatSimpleIO;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.util.List;

public class PanelTextField<T> implements IGuiPanel {
    private final IGuiRect transform;
    private boolean enabled = true;

    private IGuiTexture[] texState = new IGuiTexture[3];
    private IGuiColor[] colStates = new IGuiColor[3];
    private IGuiColor colHighlight;
    private IGuiColor colWatermark;

    private boolean lockFocus = false;
    private boolean isFocused = false;
    private boolean isActive = true;
    private boolean canWrap = false;
    private int maxLength = 32;

    private String text;
    private String watermark = "";

    private int selectStart = 0;
    private int selectEnd = 0; // WARNING: Selection end can be before selection start!
    private boolean dragging = false;
    private GuiRectangle cursorLine = new GuiRectangle(4, 4, 1, 8);

    // Yep... we're supporting this without a scrolling canvas (we don't need the zooming and mouse dragging but the scrolling bounds change much more often)
    private IValueIO<Float> scrollX;
    private IValueIO<Float> scrollY;
    private int scrollWidth = 0;
    private int scrollHeight = 0;

    private final IFieldFilter<T> filter;
    private ICallback<T> callback;

    public PanelTextField(IGuiRect rect, String text, IFieldFilter<T> filter) {
        this.transform = rect;
        cursorLine.setParent(this.transform);
        this.filter = filter;

        this.setTextures(PresetTexture.TEXT_BOX_0.getTexture(), PresetTexture.TEXT_BOX_1.getTexture(), PresetTexture.TEXT_BOX_2.getTexture());
        this.setMainColors(PresetColor.TEXT_AUX_0.getColor(), PresetColor.TEXT_AUX_0.getColor(), PresetColor.TEXT_AUX_0.getColor());
        this.setAuxColors(PresetColor.TEXT_WATERMARK.getColor(), PresetColor.TEXT_HIGHLIGHT.getColor());

        // Dummy value drivers

        scrollX = new FloatSimpleIO();
        scrollY = new FloatSimpleIO();

        this.setText(text);
    }

    public PanelTextField<T> setCallback(ICallback<T> callback) {
        this.callback = callback;
        return this;
    }

    public PanelTextField<T> setTextures(IGuiTexture disabled, IGuiTexture idle, IGuiTexture focused) {
        this.texState[0] = disabled;
        this.texState[1] = idle;
        this.texState[2] = focused;
        return this;
    }

    public PanelTextField<T> setMainColors(IGuiColor disabled, IGuiColor idle, IGuiColor focused) {
        this.colStates[0] = disabled;
        this.colStates[1] = idle;
        this.colStates[2] = focused;
        return this;
    }

    public PanelTextField<T> setAuxColors(IGuiColor watermark, IGuiColor highlight) {
        this.colWatermark = watermark;
        this.colHighlight = highlight;
        return this;
    }

    public PanelTextField<T> setMaxLength(int size) {
        this.maxLength = size;
        return this;
    }

    /**
     * Enables text wrapping for multi-line editing
     */
    public PanelTextField<T> enableWrapping(boolean state) {
        this.canWrap = state;
        updateScrollBounds();
        return this;
    }

    public void lockFocus(boolean state) {
        this.lockFocus = state;

        if (state) {
            this.isFocused = true;
        }
    }

    public PanelTextField<T> setScrollDriverX(IValueIO<Float> driver) {
        this.scrollX = driver;
        return this;
    }

    public PanelTextField<T> setScrollDriverY(IValueIO<Float> driver) {
        this.scrollY = driver;
        return this;
    }

    public int getScrollX() {
        if (scrollWidth <= 0) {
            return 0;
        }

        return (int) (scrollWidth * scrollX.readValue());
    }

    public int getScrollY() {
        if (scrollHeight <= 0) {
            return 0;
        }

        return (int) (scrollHeight * scrollY.readValue());
    }

    public void setScrollX(int value) {
        if (scrollWidth <= 0) {
            scrollX.writeValue(0F);
            return;
        }

        scrollX.writeValue(MathHelper.clamp(value, 0, scrollWidth) / (float) scrollWidth);
    }

    public void setScrollY(int value) {
        if (scrollHeight <= 0) {
            scrollY.writeValue(0F);
            return;
        }

        scrollY.writeValue(MathHelper.clamp(value, 0, scrollHeight) / (float) scrollHeight);
    }

    public void setActive(boolean state) {
        this.isActive = state;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public PanelTextField<T> setWatermark(String text) {
        this.watermark = text;
        return this;
    }

    public void setText(String text) {
        this.text = filter.filterText(text);
        updateScrollBounds();
        setCursorPosition(0);
    }

    public String getRawText() {
        return this.text;
    }

    public T getValue() {
        return this.filter.parseValue(getRawText());
    }

    public String getSelectedText() {
        int l = Math.min(selectStart, selectEnd);
        int r = Math.max(selectStart, selectEnd);
        return text.substring(l, r);
    }

    /**
     * Writes text to the current cursor position replacing any current selection
     */
    public void writeText(String in) {
        StringBuilder out = new StringBuilder();
        int l = Math.min(selectStart, selectEnd);
        int r = Math.max(selectStart, selectEnd);
        int space = this.maxLength - text.length() - (l - r);

        if (!text.isEmpty()) {
            out.append(text, 0, l);
        }

        int used;

        // Can happen if someone instantiates the field with more characters than it normally allows
        if (space < 0) {
            used = 0;
        }
        // Written string won fit
        else if (space < in.length()) {
            out.append(in, 0, space); // Cut to size
            used = space; // Mark all space as used
        } else {
            out.append(in);
            used = in.length();
        }

        if (!text.isEmpty() && r < text.length()) {
            out.append(text, r, text.length());
        }

        //if(filter.isValid(text))
        {
            this.text = filter.filterText(out.toString());
            updateScrollBounds();
            moveCursorBy(l - selectEnd + used);

            // Broadcast changes
            if (callback != null) {
                callback.setValue(filter.parseValue(this.text));
            }
        }
    }

    /**
     * Deletes the given number of whole words from the current cursor's position, unless there is currently a selection, in
     * which case the selection is deleted instead.
     */
    public void deleteWords(int num) {
        if (!this.text.isEmpty()) {
            if (this.selectEnd != this.selectStart) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.selectStart);
            }
        }
    }

    /**
     * Deletes the given number of characters from the current cursor's position, unless there is currently a selection,
     * in which case the selection is deleted instead.
     */
    public void deleteFromCursor(int num) {
        if (!this.text.isEmpty()) {
            if (this.selectEnd != this.selectStart) {
                this.writeText("");
            } else {
                boolean flag = num < 0;
                int i = flag ? this.selectStart + num : this.selectStart;
                int j = flag ? this.selectStart : this.selectStart + num;
                String s = "";

                if (i >= 0) {
                    s = this.text.substring(0, i);
                }

                if (j < this.text.length()) {
                    s = s + this.text.substring(j);
                }

                //if(filter.isValid(s))
                {
                    this.text = filter.filterText(s);

                    updateScrollBounds();

                    if (flag) {
                        this.moveCursorBy(num);
                    }

                    // Broadcast changes
                    if (callback != null) {
                        callback.setValue(filter.parseValue(this.text));
                    }
                }
            }
        }
    }

    /**
     * Gets the starting index of the word at the specified number of words away from the cursor position.
     */
    public int getNthWordFromCursor(int numWords) {
        return this.getNthWordFromPos(numWords, this.selectStart);
    }

    /**
     * Gets the starting index of the word at a distance of the specified number of words away from the given position.
     */
    public int getNthWordFromPos(int n, int pos) {
        return this.getNthWordFromPosWS(n, pos, true);
    }

    /**
     * Like getNthWordFromPos (which wraps this), but adds option for skipping consecutive spaces
     */
    public int getNthWordFromPosWS(int n, int pos, boolean skipWs) {
        int i = pos;
        boolean flag = n < 0;
        int j = Math.abs(n);

        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);

                if (i == -1) {
                    i = l;
                } else {
                    while (skipWs && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (skipWs && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursorBy(int num) {
        this.setCursorPosition(this.selectEnd + num);
    }

    /**
     * Sets the current position of the cursor.
     */
    public void setCursorPosition(int pos) {
        this.selectStart = pos;
        int i = this.text.length();
        this.selectStart = MathHelper.clamp(this.selectStart, 0, i);
        this.setSelectionPos(this.selectStart);
    }

    /**
     * Call this method from your GuiScreen to process the keys into the textbox
     */
    @Override
    public boolean onKeyTyped(char typedChar, int keyCode) {
        if (!this.isFocused) {
            return false;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            this.setCursorPosition(text.length());
            this.setSelectionPos(0);
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            if (this.isActive) {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());

            if (this.isActive) {
                this.writeText("");
            }

            return true;
        } else {
            switch (keyCode) {
                case 14: // Backspace
                {
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isActive) {
                            this.deleteWords(-1);
                        }
                    } else if (this.isActive) {
                        this.deleteFromCursor(-1);
                    }

                    return true;
                }
                case 28: // Enter
                {
                    if (canWrap) {
                        this.writeText("\n");
                    }

                    return true;
                }
                case 199: // Home
                {
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                    } else {
                        this.setCursorPosition(0);
                    }

                    return true;
                }
                case 203: // Left arrow
                {
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, selectEnd));
                        } else {
                            this.setSelectionPos(selectEnd - 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    } else {
                        this.moveCursorBy(-1);
                    }

                    return true;
                }
                case 205: // Right arrow
                {
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, selectEnd));
                        } else {
                            this.setSelectionPos(selectEnd + 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    } else {
                        this.moveCursorBy(1);
                    }

                    return true;
                }
                case 207: // End
                {
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(this.text.length());
                    } else {
                        this.setCursorPosition(text.length());
                    }

                    return true;
                }
                case 200: // Up arrow
                {
                    // TODO: Move cursor up one line
                    return true;
                }
                case 208: // Down arrow
                {
                    // TODO: Move cursor down one line
                    return true;
                }
                case 211: // Delete
                {
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isActive) {
                            this.deleteWords(1);
                        }
                    } else if (this.isActive) {
                        this.deleteFromCursor(1);
                    }

                    return true;
                }
                default: {
                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                        if (this.isActive) {
                            this.writeText(Character.toString(typedChar));
                        }

                        return true;
                    } else {
                        return false; // We're not using this key. Other controls/menus are free to use it
                    }
                }
            }
        }
    }

    /**
     * Sets the position of the selection anchor (the selection anchor and the cursor position mark the edges of the
     * selection). If the anchor is set beyond the bounds of the current text, it will be put back inside.
     */
    public void setSelectionPos(int position) {
        int i = this.text.length();

        if (position > i) {
            position = i;
        }

        if (position < 0) {
            position = 0;
        }

        if (selectEnd != position) {
            this.selectEnd = position;

            FontRenderer font = Minecraft.getMinecraft().fontRenderer;

            if (canWrap) {
                List<String> lines = RenderUtils.splitStringWithoutFormat(text, getTransform().getWidth() - 8, font);
                String lastFormat = "";
                int idx = 0;
                int y = 0;
                int x = 0;

                for (; y < lines.size(); y++) {
                    String s = lines.get(y);

                    if (selectEnd >= idx && selectEnd < idx + s.length() + (y == lines.size() - 1 ? 1 : 0)) {
                        x = RenderUtils.getStringWidth(lastFormat + s.substring(0, selectEnd - idx), font);
                        break;
                    }

                    idx += s.length();
                    lastFormat = FontRenderer.getFormatFromString(lastFormat + s);
                }

                y *= font.FONT_HEIGHT;
                int sy = getScrollY();

                if (y < sy) {
                    setScrollY(y);
                } else if (y > sy + (transform.getHeight() - 8) - font.FONT_HEIGHT) {
                    setScrollY(y - (transform.getHeight() - 8) + font.FONT_HEIGHT);
                }

                cursorLine.x = x + 4;
                cursorLine.y = y + 4;
                cursorLine.w = 1;
                cursorLine.h = font.FONT_HEIGHT;
            } else {
                int x = RenderUtils.getStringWidth(text.substring(0, selectEnd), font);
                int sx = getScrollX();

                if (x < sx) {
                    setScrollX(x);
                } else if (x > sx + (transform.getWidth() - 8)) {
                    setScrollX(x - (transform.getWidth() - 8));
                }

                cursorLine.x = x + 4;
                cursorLine.y = 4;
                cursorLine.w = 1;
                cursorLine.h = font.FONT_HEIGHT;
            }
        }
    }

    public void updateScrollBounds() {
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;

        int prevX = getScrollX();
        int prevY = getScrollY();

        if (!canWrap) {
            scrollHeight = 0;
            scrollWidth = Math.max(0, RenderUtils.getStringWidth(text, font) - (transform.getWidth() - 8));
        } else {
            scrollWidth = 0;
            scrollHeight = Math.max(0, (RenderUtils.splitString(text, transform.getWidth() - 8, font).size() * font.FONT_HEIGHT) - (transform.getHeight() - 8));
        }

        setScrollX(prevX);
        setScrollY(prevY);
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
        if (isActive && dragging && Mouse.isButtonDown(0)) {
            if (canWrap) {
                setSelectionPos(RenderUtils.getCursorPos(text, mx - (transform.getX() + 4) + getScrollX(), my - (transform.getY() + 4) + getScrollY(), transform.getWidth() - 8, Minecraft.getMinecraft().fontRenderer));
            } else {
                setSelectionPos(RenderUtils.getCursorPos(text, mx - (transform.getX() + 4) + getScrollX(), Minecraft.getMinecraft().fontRenderer));
            }
        } else if (dragging) {
            dragging = false;
        }

        IGuiRect bounds = this.getTransform();
        int state = !this.isActive() ? 0 : (isFocused ? 2 : 1);
        Minecraft mc = Minecraft.getMinecraft();
        IGuiTexture t = texState[state];
        GlStateManager.pushMatrix();

        if (t != null) // Full screen text editors probably don't need the backgrounds
        {
            t.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
        }

        RenderUtils.startScissor(bounds);
        GlStateManager.translate(-getScrollX(), -getScrollY(), 0);

        if (text.length() <= 0) {
            if (!isFocused) {
                mc.fontRenderer.drawString(watermark, bounds.getX() + 4, bounds.getY() + 4, colWatermark.getRGB(), false);
            }
        } else {
            IGuiColor c = colStates[state];

            if (!canWrap) {
                RenderUtils.drawHighlightedString(mc.fontRenderer, text, bounds.getX() + 4, bounds.getY() + 4, c.getRGB(), false, colHighlight.getRGB(), selectStart, selectEnd);
            } else {
                RenderUtils.drawHighlightedSplitString(mc.fontRenderer, text, bounds.getX() + 4, bounds.getY() + 4, bounds.getWidth() - 8, c.getRGB(), false, colHighlight.getRGB(), selectStart, selectEnd);
            }
        }

        if (isFocused && selectStart == selectEnd && (System.currentTimeMillis() / 500L) % 2 == 0) {
            RenderUtils.drawHighlightBox(cursorLine, colHighlight);
        }

        RenderUtils.endScissor();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean onMouseClick(int mx, int my, int button) {
        if (transform.contains(mx, my)) {
            if (!this.isFocused) {
                this.isFocused = true;
                updateScrollBounds(); // Just in case
            }

            if (canWrap) {
                setCursorPosition(RenderUtils.getCursorPos(text, mx - (transform.getX() + 4) + getScrollX(), my - (transform.getY() + 4) + getScrollY(), transform.getWidth() - 8, Minecraft.getMinecraft().fontRenderer));
            } else {
                setCursorPosition(RenderUtils.getCursorPos(text, mx - (transform.getX() + 4) + getScrollX(), Minecraft.getMinecraft().fontRenderer));
            }
            dragging = true;

            //return true;
        } else if (this.isFocused && !lockFocus) {
            this.isFocused = false;
            this.text = filter.parseValue(this.text).toString();
            //setCursorPosition(0);
        }

        return false;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int button) {
        return isFocused && dragging;
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        if (!isFocused || !transform.contains(mx, my)) {
            return false;
        }

        if (canWrap) {
            setScrollY(getScrollY() + (scroll * 4));
            return true;
        } /*else
        {
            // This is kinda annoying in lists
            //setScrollX(getScrollX() + (scroll * 12));
        }*/

        return false;
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        return null;
    }
}
