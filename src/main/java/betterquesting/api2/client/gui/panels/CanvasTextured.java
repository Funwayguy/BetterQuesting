package betterquesting.api2.client.gui.panels;

import net.minecraft.client.renderer.GlStateManager;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;

public class CanvasTextured extends CanvasEmpty
{
	private final IGuiTexture bgTexture;
	
	public CanvasTextured(IGuiRect rect, IGuiTexture texture)
	{
		super(rect);
		
		this.bgTexture = texture;
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		IGuiRect bounds = this.getTransform();
		GlStateManager.pushMatrix();
		GlStateManager.color(1F, 1F, 1F, 1F);
		bgTexture.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
		GlStateManager.popMatrix();
		
		super.drawPanel(mx, my, partialTick);
	}
}
