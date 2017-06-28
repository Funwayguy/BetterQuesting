package adv_director.rw2.api.client.gui.panels;

import net.minecraft.client.renderer.GlStateManager;
import adv_director.rw2.api.client.gui.resources.IGuiTexture;

public class CanvasTextured extends CanvasEmpty
{
	private final IGuiTexture bgTexture;
	
	public CanvasTextured(IGuiTexture texture)
	{
		super();
		
		this.bgTexture = texture;
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		GlStateManager.pushMatrix();
		GlStateManager.color(1F, 1F, 1F, 1F);
		bgTexture.drawTexture(this.getBounds().getX(), this.getBounds().getY(), this.getBounds().getWidth(), this.getBounds().getHeight(), 0F);
		GlStateManager.popMatrix();
		
		super.drawPanel(mx, my, partialTick);
	}
}
