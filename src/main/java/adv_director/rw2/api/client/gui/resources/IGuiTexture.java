package adv_director.rw2.api.client.gui.resources;

import org.lwjgl.util.Rectangle;
import net.minecraft.util.ResourceLocation;

public interface IGuiTexture
{
	public void drawTexture(int x, int y, int width, int height, float zDepth);
	
	public ResourceLocation getTexture();
	public Rectangle getBounds();
}
