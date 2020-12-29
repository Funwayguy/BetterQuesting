package betterquesting.api2.client.gui.controls;

import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class PanelButtonCustom extends CanvasEmpty implements IPanelButton {
    private static final ResourceLocation CLICK_SND = new ResourceLocation("gui.button.press");
    private final int buttonId;
    private boolean isActive = true;
    private Consumer<PanelButtonCustom> callback;
    private boolean isEnabled = true;
    private boolean pendingRelease;

    public PanelButtonCustom(IGuiRect transform, int buttonId) {
        super(transform);
        this.buttonId = buttonId;
    }

    @Override
    public int getButtonID() {
        return buttonId;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean state) {
        isActive = state;
    }

    @Override
    public void onButtonClick() {
        if(callback != null) callback.accept(this);
    }

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        boolean used = super.onMouseClick(mx, my, click);
        if (used) return true;

        boolean contains = this.getTransform().contains(mx, my);
        pendingRelease = isActive() && click == 0 && contains;

        return (click == 0 || click == 1) && contains;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        boolean released = super.onMouseRelease(mx, my, click);
        if (released) return true;

        if (pendingRelease) {
            pendingRelease = false;

            IGuiRect bounds = this.getTransform();
            boolean clicked = isActive() && click == 0 && bounds.contains(mx, my) && !PEventBroadcaster.INSTANCE.postEvent(new PEventButton(this));

            if (clicked) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(CLICK_SND, 1.0F));
                onButtonClick();
            }

            return clicked;
        } else {
            return false;
        }
    }

    public Consumer<PanelButtonCustom> getCallback() {
        return callback;
    }

    public void setCallback(Consumer<PanelButtonCustom> callback) {
        this.callback = callback;
    }
}
