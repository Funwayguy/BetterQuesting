package betterquesting.api2.client.gui.panels.content;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.controls.io.ValueFuncIO;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class PanelEntityPreview implements IGuiPanel {
    private final IGuiRect transform;
    private boolean enabled = true;

    public Entity entity;

    private final IValueIO<Float> basePitch;
    private final IValueIO<Float> baseYaw;
    private IValueIO<Float> pitchDriver;
    private IValueIO<Float> yawDriver;

    private float zDepth = 100F;

    public PanelEntityPreview(IGuiRect rect, Entity entity) {
        this.transform = rect;
        this.entity = entity;

        this.basePitch = new ValueFuncIO<>(() -> 15F);
        this.pitchDriver = basePitch;

        this.baseYaw = new ValueFuncIO<>(() -> -30F);
        this.yawDriver = baseYaw;
    }

    public PanelEntityPreview setRotationFixed(float pitch, float yaw) {
        this.pitchDriver = basePitch;
        this.yawDriver = baseYaw;
        basePitch.writeValue(pitch);
        baseYaw.writeValue(yaw);
        return this;
    }

    public PanelEntityPreview setRotationDriven(IValueIO<Float> pitch, IValueIO<Float> yaw) {
        this.pitchDriver = pitch == null ? basePitch : pitch;
        this.yawDriver = yaw == null ? baseYaw : yaw;
        return this;
    }

    public PanelEntityPreview setDepth(float z) {
        this.zDepth = z;
        return this;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void initPanel() {}

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

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        if (entity == null) {
            return;
        }

        IGuiRect bounds = this.getTransform();
        GL11.glPushMatrix();
        RenderUtils.startScissor(new GuiRectangle(bounds));

        GL11.glColor4f(1F, 1F, 1F, 1F);

        int sizeX = bounds.getWidth();
        int sizeY = bounds.getHeight();
        float scale = Math.min((sizeY / 2F) / entity.height, (sizeX / 2F) / entity.width);

        RenderUtils.RenderEntity(
                bounds.getX() + sizeX / 2,
                bounds.getY() + sizeY / 2 + MathHelper.ceiling_float_int(entity.height * scale / 2F),
                (int) scale,
                yawDriver.readValue(),
                pitchDriver.readValue(),
                entity);

        RenderUtils.endScissor();
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
}
