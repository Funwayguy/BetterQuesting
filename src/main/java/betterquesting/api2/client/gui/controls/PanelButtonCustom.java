package betterquesting.api2.client.gui.controls;

import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Mouse;

import java.util.function.Consumer;

public class PanelButtonCustom extends CanvasEmpty implements IPanelButton {

    private final int buttonId;
    private boolean isActive = true;
    private Consumer<PanelButtonCustom> callback;
    private boolean isEnabled = true;
    private boolean pendingRelease;
    private final IGuiTexture[] texStates = new IGuiTexture[3];

    public PanelButtonCustom(IGuiRect transform, int buttonId) {
        super(transform);
        this.buttonId = buttonId;
        this.setTextures(PresetTexture.BTN_NORMAL_0.getTexture(), PresetTexture.BTN_NORMAL_1.getTexture(), PresetTexture.BTN_NORMAL_2.getTexture());
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
    public void drawPanel(int mx, int my, float partialTick) {
        IGuiRect bounds = this.getTransform();
        int curState = !isActive()? 0 : (bounds.contains(mx, my)? 2 : 1);

        if(curState == 2 && pendingRelease && Mouse.isButtonDown(0)) {
            curState = 0;
        }

        IGuiTexture t = texStates[curState];

        if(t != null) { // Support for text or icon only buttons in one or more states.
            t.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
        }
        super.drawPanel(mx, my, partialTick);
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

        if (!pendingRelease) return false;
        pendingRelease = false;

        IGuiRect bounds = this.getTransform();
        boolean clicked = isActive() && click == 0 && bounds.contains(mx, my) && !PEventBroadcaster.INSTANCE.postEvent(new PEventButton(this));

        if (clicked) {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onButtonClick();
        }

        return clicked;
    }

    public Consumer<PanelButtonCustom> getCallback() {
        return callback;
    }

    public void setCallback(Consumer<PanelButtonCustom> callback) {
        this.callback = callback;
    }

    public PanelButtonCustom setTextures(IGuiTexture disabled, IGuiTexture idle, IGuiTexture hover)
    {
        this.texStates[0] = disabled;
        this.texStates[1] = idle;
        this.texStates[2] = hover;
        return this;
    }
}
