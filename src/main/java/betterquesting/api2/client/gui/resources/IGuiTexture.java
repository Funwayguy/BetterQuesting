package betterquesting.api2.client.gui.resources;

import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.IGuiRect;

public interface IGuiTexture
{
	public void drawTexture(int x, int y, int width, int height, float zDepth);
	
	public ResourceLocation getTexture();
	public IGuiRect getBounds();
}
