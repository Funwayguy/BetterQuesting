package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.IGuiRect;

public interface IGuiTexture
{
	void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick);
	void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color);
	
	ResourceLocation getTexture();
	IGuiRect getBounds();
}
