package betterquesting.client.ui_builder;

import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.BoxLine;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.client.gui2.editors.nbt.PanelScrollingNBT;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector4f;

import javax.annotation.Nonnull;
import java.util.*;

public class GuiBuilderMain extends GuiScreenCanvas implements IVolatileScreen {
    // Database of panel components given unique IDs
    // We can deal with the save/load here without having to make an entire class for this
    private final SimpleDatabase<ComponentPanel> COM_DB = new SimpleDatabase<>();
    // GUI panel representations of components. IDs should line up with COM_DB
    private final SimpleDatabase<IGuiPanel> PANEL_DB = new SimpleDatabase<>();

    private CanvasEmpty cvPreview;
    private IGuiCanvas cvPropTray; // Also doubles as the palette tray
    private PanelButton btnTrayToggle;

    private ResourceLocation paletteSel = null;

    private int toolMode = 0;
    // 0 = NONE (operate the preview panels as-is)
    // 1 = SELECT (highlight panels to edit/delete)
    // 2 = CREATE (Places a new panel on to the first panel under the mouse or which-ever is selected)
    // 3 = RESIZE (Drag edges of the panels to change the anchors and/or padding dimensions)
    // 4 = RE-ANCHOR (Move the panel to a new canvas parent. Ctrl moves only the transform parenting)
    // 5 = DELETE (Deletes the panel under the mouse and all of its children. Transform parents are reset to -1)

    private int selectedID = -1;
    private IGuiPanel selPn = null;

    private int dragID = -1; // Which panel are we dragging from
    private int dragType = -1; // What kind of drag are we performing

    // TODO: Add context information about what this GUI is being built for
    public GuiBuilderMain(GuiScreen parent) {
        super(parent);

        // We're using the entire screen including areas normally reserved for margins to make space for tools
        this.useMargins(false);
        this.useDefaultBG(true);
    }

    @Override
    public void initPanel() {
        super.initPanel();

        // The normal area with margins will now be the inner preview so we recalculate margins for that here
        int marginX = BQ_Settings.guiWidth <= 0 ? 16 : Math.max(16, (this.width - BQ_Settings.guiWidth) / 2);
        int marginY = BQ_Settings.guiHeight <= 0 ? 16 : Math.max(16, (this.height - BQ_Settings.guiHeight) / 2);
        GuiTransform pvTransform = new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(marginX, marginY, marginX, marginY), 0);

        cvPreview = new CanvasEmpty(pvTransform);
        this.addPanel(cvPreview);

        // === PROPERTY TRAY ===

        cvPropTray = new CanvasTextured(new GuiTransform(new Vector4f(0.5F, 0F, 1F, 1F), new GuiPadding(0, 32, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvPropTray);
        cvPropTray.setEnabled(false);

        btnTrayToggle = new PanelButton(new GuiTransform(new Vector4f(0.5F, 1F, 0.5F, 1F), -16, -16, 16, 16, 0), -1, "").setIcon(PresetIcon.ICON_UP.getTexture());
        btnTrayToggle.setClickAction((btn) -> {
            if (cvPropTray.isEnabled()) {
                closeTray();
            } else if (selectedID >= 0 && !(toolMode == 0 || toolMode == 2)) {
                ComponentPanel com = COM_DB.getValue(selectedID);
                // TODO: Add a callback here so the component can read in changes
                if (com != null) openTrayNBT(com.writeToNBT(new NBTTagCompound()));
            } else if (toolMode == 3) {
                openTrayPalette();
            }
        });
        this.addPanel(btnTrayToggle);

        // === EXIT CORNER ===

        PanelButton btnExit = new PanelButton(new GuiTransform(GuiAlign.TOP_RIGHT, -16, 0, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                mc.displayGuiScreen(parent);
            }
        }.setIcon(PresetIcon.ICON_CROSS.getTexture());
        this.addPanel(btnExit);

        // === SAVE - LOAD - REFRESH ===

        PanelButton btnSave = new PanelButton(new GuiRectangle(0, 0, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                // Deal with this later
            }
        }.setIcon(PresetIcon.ICON_TICK.getTexture());
        this.addPanel(btnSave);

        PanelButton btnLoad = new PanelButton(new GuiRectangle(16, 0, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                // Deal with this later
            }
        }.setIcon(PresetIcon.ICON_FOLDER_OPEN.getTexture());
        this.addPanel(btnLoad);

        PanelButton btnRefresh = new PanelButton(new GuiRectangle(32, 0, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                refreshComponents();
            }
        }.setIcon(PresetIcon.ICON_REFRESH.getTexture());
        this.addPanel(btnRefresh);

        // === TOOL ROW ===

        final List<PanelButton> toolBtns = new ArrayList<>();

        PanelButton btnCursor = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, 0, -16, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                toolBtns.forEach((btn) -> btn.setActive(true));
                this.setActive(false);
                toolMode = 0;
            }
        }.setIcon(PresetIcon.ICON_CURSOR.getTexture());
        btnCursor.setActive(toolMode != 0);
        this.addPanel(btnCursor);
        toolBtns.add(btnCursor);

        PanelButton btnProp = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, 16, -16, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                toolBtns.forEach((btn) -> btn.setActive(true));
                this.setActive(false);
                toolMode = 1;
            }
        }.setIcon(PresetIcon.ICON_PROPS.getTexture());
        btnProp.setActive(toolMode != 1);
        this.addPanel(btnProp);
        toolBtns.add(btnProp);

        // Adds a new panel based on where the user clicked (we can re-parent thing later so we don't need pre-selection functionality)
        PanelButton btnAdd = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, 32, -16, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                toolBtns.forEach((btn) -> btn.setActive(true));
                this.setActive(false);
                toolMode = 2;

                openTrayPalette();
            }
        }.setIcon(PresetIcon.ICON_POSITIVE.getTexture());
        btnAdd.setActive(toolMode != 2);
        this.addPanel(btnAdd);
        toolBtns.add(btnAdd);

        // Changes the canvas parent
        PanelButton btnSize = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, 48, -16, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                toolBtns.forEach((btn) -> btn.setActive(true));
                this.setActive(false);
                toolMode = 3;
            }
        }.setIcon(PresetIcon.ICON_SCALE.getTexture());
        btnSize.setActive(toolMode != 3);
        this.addPanel(btnSize);
        toolBtns.add(btnSize);

        // Edit the transform bounds
        // TODO: Add a CTRL alternate mode to move the transform parent only (doesn't need a whole new button)
        PanelButton btnLink = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, 64, -16, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                toolBtns.forEach((btn) -> btn.setActive(true));
                this.setActive(false);
                toolMode = 4;
            }
        }.setIcon(PresetIcon.ICON_LINK.getTexture());
        btnLink.setActive(toolMode != 4);
        this.addPanel(btnLink);
        toolBtns.add(btnLink);

        // Delete the panel and its children
        PanelButton btnDel = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, 80, -16, 16, 16, 0), -1, "") {
            @Override
            public void onButtonClick() {
                toolBtns.forEach((btn) -> btn.setActive(true));
                this.setActive(false);
                toolMode = 5;
            }
        }.setIcon(PresetIcon.ICON_TRASH.getTexture());
        btnDel.setActive(toolMode != 5);
        this.addPanel(btnDel);
        toolBtns.add(btnDel);

        refreshComponents();
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        super.drawPanel(mx, my, partialTick);

        int cmx = MathHelper.clamp(mx, cvPreview.getTransform().getX(), cvPreview.getTransform().getX() + cvPreview.getTransform().getWidth());
        int cmy = MathHelper.clamp(my, cvPreview.getTransform().getY(), cvPreview.getTransform().getY() + cvPreview.getTransform().getHeight());

        // === DRAG ACTIONS ===

        if (dragID >= 0) {
            if (toolMode == 3 && selPn != null && dragType > 0) {
                ComponentPanel com = COM_DB.getValue(selectedID);

                if ((dragType & 16) == 16 && selPn.getTransform() instanceof GuiTransform) // Editing anchor
                {
                    GuiTransform trans = (GuiTransform) selPn.getTransform();
                    int dx = cmx - trans.getParent().getX();
                    int dy = cmy - trans.getParent().getY();
                    int width = trans.getParent().getWidth();
                    int height = trans.getParent().getHeight();
                    boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

                    // Edge Bit Map = R L B T
                    if ((dragType & 1) == 1 && height > 0) {
                        trans.getAnchor().y = dy / (float) height;
                        if (shift) {
                            trans.getAnchor().y += 0.025F;
                            trans.getAnchor().y -= trans.getAnchor().y % 0.05F;
                        }
                    }

                    if ((dragType & 2) == 2 && height > 0) {
                        trans.getAnchor().w = dy / (float) height;
                        if (shift) {
                            trans.getAnchor().w += 0.025F;
                            trans.getAnchor().w -= trans.getAnchor().w % 0.05F;
                        }
                    }

                    if ((dragType & 4) == 4 && width > 0) {
                        trans.getAnchor().x = dx / (float) width;
                        if (shift) {
                            trans.getAnchor().x += 0.025F;
                            trans.getAnchor().x -= trans.getAnchor().x % 0.05F;
                        }
                    }

                    if ((dragType & 8) == 8 && width > 0) {
                        trans.getAnchor().z = dx / (float) width;
                        if (shift) {
                            trans.getAnchor().z += 0.025F;
                            trans.getAnchor().z -= trans.getAnchor().z % 0.05F;
                        }
                    }

                    if (com != null) com.setTransform(trans);
                } else if ((dragType & 16) == 0 && selPn.getTransform() instanceof GuiTransform) {
                    // TODO: Make this work for GuiRectangle (or just deprecate that class entirely... but legacy support uhg)
                    GuiTransform trans = (GuiTransform) selPn.getTransform();
                    int width = MathHelper.ceil(trans.getParent().getWidth() * (trans.getAnchor().z - trans.getAnchor().x));
                    int height = MathHelper.ceil(trans.getParent().getHeight() * (trans.getAnchor().w - trans.getAnchor().y));
                    int dx = cmx - (trans.getParent().getX() + MathHelper.ceil(trans.getParent().getWidth() * trans.getAnchor().x));
                    int dy = cmy - (trans.getParent().getY() + MathHelper.ceil(trans.getParent().getHeight() * trans.getAnchor().y));
                    boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

                    // Edge Bit Map = R L B T
                    if ((dragType & 1) == 1) {
                        trans.getPadding().t = dy;
                        if (shift) {
                            trans.getPadding().t += 4;
                            trans.getPadding().t -= trans.getPadding().t % 8;
                        }
                    }

                    if ((dragType & 2) == 2) {
                        trans.getPadding().b = height - dy;
                        if (shift) {
                            trans.getPadding().b += 4;
                            trans.getPadding().b -= trans.getPadding().b % 8;
                        }
                    }

                    if ((dragType & 4) == 4) {
                        trans.getPadding().l = dx;
                        if (shift) {
                            trans.getPadding().l += 4;
                            trans.getPadding().l -= trans.getPadding().l % 8;
                        }
                    }

                    if ((dragType & 8) == 8) {
                        trans.getPadding().r = width - dx;
                        if (shift) {
                            trans.getPadding().r += 4;
                            trans.getPadding().r -= trans.getPadding().r % 8;
                        }
                    }

                    if (com != null) com.setTransform(trans);
                }
            }

            if (!Mouse.isButtonDown(0)) {
                if (dragID >= 0) refreshComponents();
                dragID = -1;
                dragType = -1;
            }
        }

        // === HOVER ACTIONS ===

        if (toolMode == 2 && cvPreview.getTransform().contains(mx, my)) {
            IGuiPanel topPanel = getPanelUnderMouse(mx, my, 0);
            if (topPanel == null) topPanel = cvPreview;

            IGuiRect drawBounds;

            int edge = getNearbyEdges(topPanel.getTransform(), mx, my, 16, true);

            if (edge > 0) {
                drawBounds = PRE_TF_EDGE.get(edge);
                drawBounds.setParent(topPanel.getTransform());
            } else {
                int segment = getSegmentSlice(topPanel.getTransform(), mx, my);
                drawBounds = PRE_TF_SEG[segment];
                drawBounds.setParent(topPanel.getTransform());
            }

            drawTransformBounds(drawBounds, false);
        } else if ((toolMode == 1 || toolMode == 3) && selPn != null) {
            drawTransformBounds(selPn.getTransform(), false);
        }
    }

    private final GuiTransform refBounds = new GuiTransform();

    // I don't think I need to theme these. Not hard to change later if necessary
    private BoxLine boxLine = new BoxLine();
    private IGuiColor parCol = new GuiColorStatic(0xFFFF0000);
    private IGuiColor ancCol = new GuiColorStatic(0xFFFFFF00);
    private IGuiColor boundsCol = new GuiColorStatic(0xFF0000FF);

    private void drawTransformBounds(@Nonnull IGuiRect rect, boolean showNumbers) {
        if (rect.getParent() != null) boxLine.drawLine(rect.getParent(), rect.getParent(), 2, parCol, 1F);
        boxLine.drawLine(rect, rect, 2, boundsCol, 1F);

        if (rect.getParent() == null) return;

        int midX = rect.getX() + rect.getWidth() / 2;
        int midY = rect.getY() + rect.getHeight() / 2;

        int x1 = rect.getParent().getX();
        int x2 = rect.getX();
        int x3 = x2 + rect.getWidth();
        int x4 = rect.getParent().getX() + rect.getParent().getWidth();
        int ax1 = x1;
        int ax2 = x4;

        int y1 = rect.getParent().getY();
        int y2 = rect.getY();
        int y3 = y2 + rect.getHeight();
        int y4 = rect.getParent().getY() + rect.getParent().getHeight();
        int ay1 = y1;
        int ay2 = y4;

        if (rect instanceof GuiTransform) {
            GuiTransform trans = (GuiTransform) rect;
            ax1 = x1 + MathHelper.floor(trans.getParent().getWidth() * trans.getAnchor().x);
            ax2 = x1 + MathHelper.floor(trans.getParent().getWidth() * trans.getAnchor().z);

            ay1 = y1 + MathHelper.floor(trans.getParent().getHeight() * trans.getAnchor().y);
            ay2 = y1 + MathHelper.floor(trans.getParent().getHeight() * trans.getAnchor().w);

            // X axis line
            drawSimpleLine(x1, midY, ax1, midY, 2, ancCol);
            drawSimpleLine(ax2, midY, x4, midY, 2, ancCol);

            // X axis cross bar
            drawSimpleLine(ax1, midY - 8, ax1, midY + 8, 2, ancCol);
            drawSimpleLine(ax2, midY - 8, ax2, midY + 8, 2, ancCol);

            // Y axis line
            drawSimpleLine(midX, y1, midX, ay1, 2, ancCol);
            drawSimpleLine(midX, ay2, midX, y4, 2, ancCol);

            // Y axis cross bar
            drawSimpleLine(midX - 8, ay1, midX + 8, ay1, 2, ancCol);
            drawSimpleLine(midX - 8, ay2, midX + 8, ay2, 2, ancCol);
        }

        drawSimpleLine(ax1, midY, x2, midY, 2, parCol);
        drawSimpleLine(x3, midY, ax2, midY, 2, parCol);

        drawSimpleLine(midX, ay1, midX, y2, 2, parCol);
        drawSimpleLine(midX, y3, midX, ay2, 2, parCol);
    }

    private void drawSimpleLine(int x1, int y1, int x2, int y2, int width, IGuiColor color) {
        GlStateManager.pushMatrix();

        GlStateManager.disableTexture2D();
        color.applyGlColor();
        GL11.glLineWidth(width);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();

        GL11.glLineWidth(1F);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1F, 1F, 1F, 1F);

        GlStateManager.popMatrix();
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        if (toolMode == 0 || !cvPreview.getTransform().contains(mx, my)) return super.onMouseClick(mx, my, click);

        if (toolMode == 1) {
            if (click == 0) {
                IGuiPanel topPanel = getPanelUnderMouse(mx, my, 0); // Has a bit of give in it so tiny panels can still be grabbed
                selectedID = PANEL_DB.getID(topPanel);
                if (selectedID >= 0) selPn = topPanel;
            } else if (click == 1) {
                selPn = null;
                selectedID = -1;
            }
        } else if (toolMode == 2 && click == 0 && paletteSel != null) // Create
        {
            IGuiPanel topPanel = getPanelUnderMouse(mx, my, 0);
            if (topPanel == null) topPanel = cvPreview;

            GuiTransform drawBounds;

            int edge = getNearbyEdges(topPanel.getTransform(), mx, my, 16, true);

            if (edge > 0) {
                drawBounds = PRE_TF_EDGE.get(edge);
                drawBounds.setParent(topPanel.getTransform());
            } else {
                int segment = getSegmentSlice(topPanel.getTransform(), mx, my);
                drawBounds = PRE_TF_SEG[segment];
                drawBounds.setParent(topPanel.getTransform());
            }

            drawBounds = drawBounds.copy();
            if (topPanel == cvPreview) {
                drawBounds.getPadding().setPadding(0, 0, 0, 0);
            } else {
                drawBounds.getPadding().setPadding(8, 8, 8, 8);
            }

            ComponentPanel com = new ComponentPanel(drawBounds);
            COM_DB.add(COM_DB.nextID(), com);

            com.panelType = paletteSel.toString();
            //com.setPanelData(ComponentRegistry.INSTANCE.getTemplateNbt(paletteSel));

            int id = PANEL_DB.getID(topPanel);
            selectedID = id; // Quick select this new panel
            com.cvParentID = id;

            refreshComponents();

            selPn = PANEL_DB.getValue(id);
        } else if (toolMode == 3 && cvPreview.getChildren().size() > 0) // Resize
        {
            if (click == 0 && selPn == null) {
                IGuiPanel topPanel = getPanelUnderMouse(mx, my, 0); // Has a bit of give in it so tiny panels can still be grabbed
                selectedID = PANEL_DB.getID(topPanel);
                if (selectedID >= 0) selPn = topPanel;
            } else if (click == 0) {
                int edgePad = getNearbyEdges(selPn.getTransform(), mx, my, 4, true);
                int edgePar = 0;

                if (edgePad <= 0 && selPn.getTransform() instanceof GuiTransform) {
                    // We want a version of the transform without the padding but still keeping anchor offsets (because that's what we're grabbing
                    GuiTransform temp = new GuiTransform(((GuiTransform) selPn.getTransform()).getAnchor(), new GuiPadding(0, 0, 0, 0), 0);
                    temp.setParent(selPn.getTransform().getParent());
                    edgePar = selPn.getTransform().getParent() == null ? 0 : getNearbyEdges(temp, mx, my, 4, false);
                }

                if (edgePad > 0) {
                    dragID = selectedID;
                    dragType = edgePad;
                } else if (edgePar > 0) {
                    dragID = selectedID;
                    dragType = edgePar | 16; // Offset indicates anchor adjustments
                }
            } else if (click == 1) {
                selPn = null;
                selectedID = -1;
            }
        } else if (toolMode == 5 && cvPreview.getTransform().contains(mx, my)) // Delete
        {
            IGuiPanel topPanel = getPanelUnderMouse(mx, my, 0);
            int topID = topPanel == null ? -1 : PANEL_DB.getID(topPanel);

            if (topID >= 0) {
                removeComponent(topID);
                refreshComponents();
            }
        }

        return super.onMouseClick(mx, my, click);
    }

    @Override
    public boolean onKeyTyped(char c, int keycode) {
        return super.onKeyTyped(c, keycode);
    }

    private void refreshComponents() {
        PANEL_DB.reset();
        cvPreview.resetCanvas();

        // Instantiate all the panels first
        COM_DB.getEntries().forEach((entry) -> {
            IGuiPanel pan = entry.getValue().build();
            if (pan != null) PANEL_DB.add(entry.getID(), pan); // Should never be null but for my sanity...
        });

        // Rebuild from back to front (this is a part is bit annoying) we can also do the transform parenting here too
        Queue<Integer> parentQueue = new ArrayDeque<>();
        parentQueue.add(-1);
        while (!parentQueue.isEmpty()) {
            int pID = parentQueue.poll();
            IGuiPanel pPan = PANEL_DB.getValue(pID);
            if (pPan == null) pPan = cvPreview;

            if (pPan instanceof IGuiCanvas) // Skip if we can't parent it
            {
                IGuiCanvas pCan = ((IGuiCanvas) pPan);
                getCanvasChildren(pID).forEach((entry) -> {
                    pCan.addPanel(PANEL_DB.getValue(entry.getID()));
                    parentQueue.add(entry.getID()); // Queue these up for the next layer pass
                });
            }

            // Transform parenting (must occur after canvas parenting)
            ComponentPanel com = COM_DB.getValue(pID);
            IGuiPanel tPan = com == null ? null : PANEL_DB.getValue(com.tfParentID);
            if (tPan != null) pPan.getTransform().setParent(tPan.getTransform());
        }

        if (selectedID >= 0) {
            selPn = PANEL_DB.getValue(selectedID);
            if (selPn == null) selectedID = -1;
        }
    }

    private void removeComponent(int comID) {
        Queue<Integer> parentQueue = new ArrayDeque<>();
        parentQueue.add(comID);

        while (!parentQueue.isEmpty()) {
            int pID = parentQueue.poll();

            getCanvasChildren(pID).forEach((entry) -> parentQueue.add(entry.getID()));
            getTransformChildren(pID).forEach((entry) -> entry.getValue().tfParentID = -1);

            COM_DB.removeID(pID);
            PANEL_DB.removeID(pID);
        }
    }

    @Nonnull
    private List<DBEntry<ComponentPanel>> getCanvasChildren(int panelID) {
        List<DBEntry<ComponentPanel>> list = new ArrayList<>();

        COM_DB.getEntries().forEach((entry) -> {
            if (entry.getValue().cvParentID == panelID) list.add(entry);
        });

        return list;
    }

    @Nonnull
    private List<DBEntry<ComponentPanel>> getTransformChildren(int panelID) {
        List<DBEntry<ComponentPanel>> list = new ArrayList<>();

        COM_DB.getEntries().forEach((entry) -> {
            if (entry.getValue().tfParentID == panelID) list.add(entry);
        });

        return list;
    }

    private IGuiPanel getPanelUnderMouse(int mx, int my, int range) {
        ListIterator<IGuiPanel> iter = cvPreview.getChildren().listIterator(cvPreview.getChildren().size());
        IGuiPanel topPanel = null;

        while (iter.hasPrevious()) {
            IGuiPanel pan = iter.previous();
            if (containsRanged(pan.getTransform(), mx, my, range)) // Note: We don't care if this is clickable or not. We just care about the bounds
            {
                topPanel = pan;
                if (pan instanceof IGuiCanvas) // Check if there are any children here that we need
                {
                    iter = ((IGuiCanvas) pan).getChildren().listIterator(((IGuiCanvas) pan).getChildren().size());
                    continue;
                }
                break;
            }
        }

        return topPanel;
    }

    private void openTrayNBT(NBTTagCompound tag) {
        cvPropTray.setEnabled(true);
        cvPropTray.resetCanvas();

        btnTrayToggle.setIcon(PresetIcon.ICON_DOWN.getTexture());

        // TODO: This panel doesn't work on its own
        PanelScrollingNBT cvNbt = new PanelScrollingNBT(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 8, 0), 0), tag, 1, 2, 3, 4);
        cvPropTray.addPanel(cvNbt);

        PanelVScrollBar scPropTray = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 0, 0, 0), 0));
        cvNbt.setScrollDriverY(scPropTray);
        cvPropTray.addPanel(scPropTray);
    }

    private void openTrayPalette() {
        cvPropTray.setEnabled(true);
        cvPropTray.resetCanvas();

        btnTrayToggle.setIcon(PresetIcon.ICON_DOWN.getTexture());

        CanvasScrolling cvScroll = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 8, 0), 0));
        cvPropTray.addPanel(cvScroll);

        PanelVScrollBar scPropTray = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 0, 0, 0), 0));
        cvScroll.setScrollDriverY(scPropTray);
        cvPropTray.addPanel(scPropTray);

        final int cvWidth = cvScroll.getTransform().getWidth();
        List<ResourceLocation> resList = ComponentRegistry.INSTANCE.getRegisteredIDs();
        for (int i = 0; i < resList.size(); i++) {
            ResourceLocation res = resList.get(i);
            PanelButtonStorage<ResourceLocation> btnID = new PanelButtonStorage<>(new GuiRectangle(0, i * 16, cvWidth, 16, 0), -1, res.toString(), res);
            btnID.setCallback((id) -> paletteSel = id);
            cvScroll.addPanel(btnID);
        }
    }

    private void closeTray() {
        cvPropTray.resetCanvas();
        cvPropTray.setEnabled(false);

        btnTrayToggle.setIcon(PresetIcon.ICON_UP.getTexture());
    }

    private int getNearbyEdges(IGuiRect rect, int mx, int my, int range, boolean bounded) {
        int value = 0;

        boolean boundX = mx >= rect.getX() && mx < rect.getX() + rect.getWidth();
        boolean boundY = my >= rect.getY() && my < rect.getY() + rect.getHeight();

        // Bottom - Top (bits 1 & 2)
        if (Math.abs(my - (rect.getY() + rect.getHeight())) <= range && (!bounded || boundX)) {
            value |= 2;
        } else if (Math.abs(my - rect.getY()) <= range && mx >= rect.getX() && (!bounded || boundX)) {
            value |= 1;
        }

        // Right - Left (bits 4 & 8)
        if (Math.abs(mx - (rect.getX() + rect.getWidth())) <= range && (!bounded || boundY)) {
            value |= 8;
        } else if (Math.abs(mx - rect.getX()) <= range && my >= rect.getY() && (!bounded || boundY)) {
            value |= 4;
        }

        return value;
    }

    private int getSegmentSlice(IGuiRect rect, int mx, int my) {
        if (!rect.contains(mx, my)) return -1;

        int dx = mx - rect.getX();
        int dy = my - rect.getY();

        int segX = dx / (rect.getWidth() / 3);
        int segY = dy / (rect.getHeight() / 3);

        return segY * 3 + segX;
    }

    // Similar to the one built into IGuiRect but with an aditional range added on
    private boolean containsRanged(IGuiRect rect, int x3, int y3, int range) {
        int x1 = rect.getX();
        int y1 = rect.getY();
        int w = rect.getWidth();
        int h = rect.getHeight();
        int x2 = x1 + w;
        int y2 = y1 + h;
        return x3 >= x1 - range && x3 < x2 + range && y3 >= y1 - range && y3 < y2 + range;
    }

    private static final GuiTransform[] PRE_TF_SEG = new GuiTransform[]{
            new GuiTransform(new Vector4f(0F, 0F, 0.5F, 0.5F), new GuiPadding(0, 0, 0, 0), 0),
            new GuiTransform(new Vector4f(0F, 0F, 1F, 0.5F), new GuiPadding(0, 0, 0, 0), 0),
            new GuiTransform(new Vector4f(0.5F, 0F, 1F, 0.5F), new GuiPadding(0, 0, 0, 0), 0),

            new GuiTransform(new Vector4f(0F, 0F, 0.5F, 1F), new GuiPadding(0, 0, 0, 0), 0),
            new GuiTransform(new Vector4f(0F, 0F, 1F, 1F), new GuiPadding(0, 0, 0, 0), 0),
            new GuiTransform(new Vector4f(0.5F, 0F, 1F, 1F), new GuiPadding(0, 0, 0, 0), 0),

            new GuiTransform(new Vector4f(0F, 0.5F, 0.5F, 1F), new GuiPadding(0, 0, 0, 0), 0),
            new GuiTransform(new Vector4f(0F, 0.5F, 1F, 1F), new GuiPadding(0, 0, 0, 0), 0),
            new GuiTransform(new Vector4f(0.5F, 0.5F, 1F, 1F), new GuiPadding(0, 0, 0, 0), 0)
    };

    private static final Map<Integer, GuiTransform> PRE_TF_EDGE;

    static {
        HashMap<Integer, GuiTransform> tmp = new HashMap<>();

        // Edge Bit Map = R L B T

        //Corners
        tmp.put(0b0101, new GuiTransform(GuiAlign.TOP_LEFT, new GuiPadding(-4, -4, -4, -4), 0));
        tmp.put(0b1001, new GuiTransform(GuiAlign.TOP_RIGHT, new GuiPadding(-4, -4, -4, -4), 0));
        tmp.put(0b1010, new GuiTransform(GuiAlign.BOTTOM_RIGHT, new GuiPadding(-4, -4, -4, -4), 0));
        tmp.put(0b0110, new GuiTransform(GuiAlign.BOTTOM_LEFT, new GuiPadding(-4, -4, -4, -4), 0));

        //Edges
        tmp.put(0b0001, new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(-4, -4, -4, -4), 0));
        tmp.put(0b1000, new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-4, -4, -4, -4), 0));
        tmp.put(0b0010, new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(-4, -4, -4, -4), 0));
        tmp.put(0b0100, new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(-4, -4, -4, -4), 0));

        PRE_TF_EDGE = Collections.unmodifiableMap(tmp);
    }
}
