package betterquesting.api2.client.gui.popups;

import betterquesting.api2.client.gui.SceneController;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.ColorTexture;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import org.lwjgl.util.vector.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PopMessage extends CanvasEmpty {
    private final String message;
    private final IGuiTexture icon;

    public PopMessage(@Nonnull String message) {
        this(message, null);
    }

    public PopMessage(@Nonnull String message, @Nullable IGuiTexture icon) {
        super(new GuiTransform(GuiAlign.FULL_BOX));
        this.message = message;
        this.icon = icon;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 1), new ColorTexture(new GuiColorStatic(0x80000000))));

        CanvasTextured cvBox = new CanvasTextured(new GuiTransform(new Vector4f(0.2F, 0.3F, 0.8F, 0.6F)), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBox);

        if (icon != null) {
            CanvasTextured icoFrame = new CanvasTextured(new GuiTransform(new Vector4f(0.5F, 0.3F, 0.5F, 0.3F), -16, -40, 32, 32, 0), PresetTexture.PANEL_MAIN.getTexture());
            this.addPanel(icoFrame);

            icoFrame.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 8, 8), 0), icon));
        }

        cvBox.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 8, 8), 0), message).setAlignment(1));
        PanelButton btn = new PanelButton(new GuiTransform(new Vector4f(0.5F, 0.6F, 0.5F, 0.6F), -48, 8, 96, 16, 0), -1, QuestTranslation.translate("gui.back"));
        btn.setClickAction((b) -> {
            if (SceneController.getActiveScene() != null) SceneController.getActiveScene().closePopup();
        });
        this.addPanel(btn);
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
