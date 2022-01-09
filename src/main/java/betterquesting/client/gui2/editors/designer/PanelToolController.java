package betterquesting.client.gui2.editors.designer;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.controls.io.FloatSimpleIO;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.api2.client.gui.resources.colors.GuiColorPulse;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.BoxLine;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.ColorTexture;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Kinda just a poxy panel where tools can be hotswapped out
public class PanelToolController implements IGuiPanel {
    private CanvasQuestLine questLine;
    private final IGuiRect transform;
    private boolean enabled = true;

    private final IValueIO<Float> scDriverX;
    private final IValueIO<Float> scDriverY;

    private IToolboxTool activeTool;

    public static final NonNullList<PanelButtonQuest> selected = NonNullList.create();
    public static final List<IGuiPanel> highlights = new ArrayList<>();
    private GuiRectangle selBounds;

    private final IGuiLine selLine = new BoxLine();
    private final IGuiColor selCol = new GuiColorPulse(0xFFFFFFFF, 0xFF000000, 2F, 0F);
    private final IGuiTexture hTex = new ColorTexture(new GuiColorPulse(0x22FFFFFF, 0x77FFFFFF, 2F, 0F));

    public PanelToolController(IGuiRect rect, CanvasQuestLine questLine) {
        this.transform = rect;
        this.questLine = questLine;

        scDriverX = new FloatSimpleIO() {
            @Override
            public void writeValue(Float value) {
                if (activeTool != null && !activeTool.clampScrolling()) {
                    this.v = value;
                } else {
                    this.v = MathHelper.clamp(value, 0F, 1F);
                }
            }
        }.setLerp(false, 0.02F);

        scDriverY = new FloatSimpleIO() {
            @Override
            public void writeValue(Float value) {
                if (activeTool != null && !activeTool.clampScrolling()) {
                    this.v = value;
                } else {
                    this.v = MathHelper.clamp(value, 0F, 1F);
                }
            }
        }.setLerp(false, 0.02F);
    }

    public void setActiveTool(IToolboxTool tool) {
        if (this.activeTool != null) activeTool.disableTool();
        if (tool == null) return;

        activeTool = tool;
        tool.initTool(questLine);
    }

    public IToolboxTool getActiveTool() {
        return this.activeTool;
    }

    public void changeCanvas(@Nonnull CanvasQuestLine canvas) {
        this.questLine = canvas;
        refreshCanvas();
        setActiveTool(getActiveTool());
    }

    public void refreshCanvas() {
        List<PanelButtonQuest> tmp = new ArrayList<>();
        for (PanelButtonQuest b1 : selected) {
            for (PanelButtonQuest b2 : questLine.getQuestButtons())
                if (b1.getStoredValue().getID() == b2.getStoredValue().getID()) tmp.add(b2);
        }

        selected.clear();
        selected.addAll(tmp);

        highlights.clear();
        for (PanelButtonQuest btn : selected) highlights.add(new PanelGeneric(btn.rect, hTex));

        if (this.activeTool != null) activeTool.refresh(this.questLine);
    }

    public CanvasQuestLine getCanvas() {
        return this.getCanvas();
    }

    public IValueIO<Float> getScrollX() {
        return this.scDriverX;
    }

    public IValueIO<Float> getScrollY() {
        return this.scDriverY;
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
        return enabled;
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        if (!enabled) return;

        if (activeTool != null) {
            float zs = questLine.getZoom();
            int lsx = questLine.getScrollX();
            int lsy = questLine.getScrollY();
            int tx = getTransform().getX();
            int ty = getTransform().getY();
            int smx = (int) ((mx - tx) / zs) + lsx;
            int smy = (int) ((my - ty) / zs) + lsy;

            GlStateManager.pushMatrix();
            RenderUtils.startScissor(transform);

            GlStateManager.translate(tx - lsx * zs, ty - lsy * zs, 0F);
            GlStateManager.scale(zs, zs, zs);

            if (selBounds != null) {
                selBounds.w = smx - selBounds.x;
                selBounds.h = smy - selBounds.y;

                selLine.drawLine(selBounds, selBounds, 2, selCol, partialTick);
            }

            for (IGuiPanel pn : highlights) pn.drawPanel(smx, smy, partialTick);

            // Pretending we're on the scrolling canvas (when we're really not) so as not to influence it by hotswapping panels
            activeTool.drawCanvas(smx, smy, partialTick);

            RenderUtils.endScissor();
            GlStateManager.popMatrix();

            activeTool.drawOverlay(mx, my, partialTick);
        }
    }

    @Override
    public boolean onMouseClick(int mx, int my, int button) {
        if (activeTool != null && this.getTransform().contains(mx, my)) {
            if (activeTool.onMouseClick(mx, my, button)) return true;
            if (activeTool.useSelection()) {
                if (button == 1) {
                    selBounds = null;
                    selected.clear();
                    highlights.clear();
                    activeTool.onSelection(selected);
                    return true;
                } else if (button == 0) {
                    float zs = questLine.getZoom();
                    int lsx = questLine.getScrollX();
                    int lsy = questLine.getScrollY();
                    int tx = questLine.getTransform().getX();
                    int ty = questLine.getTransform().getY();
                    int smx = (int) ((mx - tx) / zs) + lsx;
                    int smy = (int) ((my - ty) / zs) + lsy;

                    selBounds = new GuiRectangle(smx, smy, 0, 0);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int button) {
        if (selBounds != null) {
            if (selBounds.w < 0) {
                selBounds.x += selBounds.w;
                selBounds.w *= -1;
            }

            if (selBounds.h < 0) {
                selBounds.y += selBounds.h;
                selBounds.h *= -1;
            }

            boolean append = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            boolean subtract = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

            if (!append && !subtract) selected.clear();

            for (PanelButtonQuest btn : questLine.getQuestButtons()) {
                if (selBounds.contains(btn.rect.x + btn.rect.w / 2, btn.rect.y + btn.rect.h / 2) || (btn.rect.contains(selBounds.x, selBounds.y) && Math.max(selBounds.w, selBounds.h) < 4)) {
                    if (subtract) {
                        selected.remove(btn);
                        continue;
                    }
                    if (append && selected.contains(btn)) continue;
                    selected.add(btn);
                }
            }

            highlights.clear();
            for (PanelButtonQuest btn : selected) highlights.add(new PanelGeneric(btn.rect, hTex));

            selBounds = null;
            if (activeTool != null) activeTool.onSelection(selected);
        }

        if (activeTool != null) return activeTool.onMouseRelease(mx, my, button);
        return false;
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        if (activeTool != null) return activeTool.onMouseScroll(mx, my, scroll);
        return false;
    }

    @Override
    public boolean onKeyTyped(char c, int keycode) {
        if (activeTool != null) {
            if (activeTool.onKeyPressed(c, keycode)) return true;
            if (activeTool.useSelection() && keycode == Keyboard.KEY_A) {
                boolean append = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
                boolean subtract = append && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));

                if (subtract) {
                    selBounds = null;
                    selected.clear();
                    highlights.clear();
                    activeTool.onSelection(selected);
                    return true;
                } else if (append) {
                    selected.clear();
                    highlights.clear();

                    for (PanelButtonQuest btn : questLine.getQuestButtons()) {
                        selected.add(btn);
                        highlights.add(new PanelGeneric(btn.rect, hTex));
                    }

                    selBounds = null;
                    activeTool.onSelection(selected);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        if (selBounds != null) return Collections.emptyList();
        if (activeTool != null) return activeTool.getTooltip(mx, my);
        return null;
    }
}
