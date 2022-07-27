package betterquesting.api2.client.gui.panels;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import org.lwjgl.opengl.GL11;

public class CanvasTextured extends CanvasEmpty {
    private final IGuiTexture bgTexture;

    public CanvasTextured(IGuiRect rect, IGuiTexture texture) {
        super(rect);

        this.bgTexture = texture;
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        IGuiRect bounds = this.getTransform();
        GL11.glPushMatrix();
        GL11.glColor4f(1F, 1F, 1F, 1F);
        bgTexture.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
        GL11.glPopMatrix();

        super.drawPanel(mx, my, partialTick);
    }
}
