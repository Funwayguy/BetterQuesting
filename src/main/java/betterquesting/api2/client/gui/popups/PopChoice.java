package betterquesting.api2.client.gui.popups;

import betterquesting.api2.client.gui.SceneController;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasResizeable;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.ColorTexture;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import org.lwjgl.util.vector.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class PopChoice extends CanvasEmpty {
    private final String message;
    private final IGuiTexture icon;
    private final Consumer<Integer> callback;
    private final String[] options;

    public PopChoice(@Nonnull String message, @Nonnull Consumer<Integer> callback, @Nonnull String... options) {
        this(message, null, callback, options);
    }

    public PopChoice(@Nonnull String message, @Nullable IGuiTexture icon, @Nonnull Consumer<Integer> callback, @Nonnull String... options) {
        super(new GuiTransform(GuiAlign.FULL_BOX));
        this.message = message;
        this.icon = icon;
        this.callback = callback;
        this.options = options;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 1), new ColorTexture(new GuiColorStatic(0x80000000))));

        CanvasResizeable cvBox = new CanvasResizeable(new GuiTransform(new Vector4f(0.5F, 0.45F, 0.5F, 0.45F)), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBox);
        cvBox.lerpToRect(new GuiTransform(new Vector4f(0.2F, 0.3F, 0.8F, 0.6F)), 200L, true);

        if (icon != null) {
            CanvasTextured icoFrame = new CanvasTextured(new GuiTransform(new Vector4f(0.5F, 0.3F, 0.5F, 0.3F), -16, -40, 32, 32, 0), PresetTexture.PANEL_MAIN.getTexture());
            this.addPanel(icoFrame);

            icoFrame.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 8, 8), 0), icon));
        }

        cvBox.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 8, 8), 0), message).setAlignment(1));

        final int maxW = 3;
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            int rowY = i / maxW;
            int rowX = i % maxW;
            int rowW = Math.min(3, options.length - (rowY * maxW)) * 112 - 16;

            PanelButton btn = new PanelButton(new GuiTransform(new Vector4f(0.5F, 0.6F, 0.5F, 0.6F), -rowW / 2 + rowX * 112, 8 + 24 * rowY, 96, 16, 0), -1, options[i]);
            btn.setClickAction((b) -> {
                callback.accept(index);
                if (SceneController.getActiveScene() != null) SceneController.getActiveScene().closePopup();
            });
            this.addPanel(btn);
        }
    }

    // == TRAP ALL UI USAGE UNTIL CLOSED ===

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        super.onMouseClick(mx, my, click);

        return true;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        super.onMouseRelease(mx, my, click);

        return true;
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        super.onMouseScroll(mx, my, scroll);

        return true;
    }

    @Override
    public boolean onKeyTyped(char c, int keycode) {
        super.onKeyTyped(c, keycode);

        return true;
    }
}
