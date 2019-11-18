package betterquesting.api2.client.gui.panels;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import net.minecraft.client.renderer.GlStateManager;

public class CanvasTextured extends CanvasEmpty
{
	private IGuiTexture bgTexture;
	
	public CanvasTextured(IGuiRect rect, IGuiTexture texture)
	{
		super(rect);
		
		this.bgTexture = texture;
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
	    if(bgTexture == null) return;
	    
		IGuiRect bounds = this.getTransform();
		GlStateManager.pushMatrix();
		GlStateManager.color(1F, 1F, 1F, 1F);
		bgTexture.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
		GlStateManager.popMatrix();
		
		super.drawPanel(mx, my, partialTick);
	}
}
