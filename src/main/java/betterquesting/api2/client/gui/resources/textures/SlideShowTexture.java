package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.IGuiRect;

public class SlideShowTexture implements IGuiTexture
{
	private final IGuiTexture[] slides;
	private final float interval;
	
	public SlideShowTexture(float interval, IGuiTexture... slides)
	{
		this.slides = slides;
		this.interval = interval;
	}
	
	@Override
	public void drawTexture(int x, int y, int width, int height, float zLevel, float partialTick)
	{
		getCurrentFrame().drawTexture(x, y, width, height, zLevel, partialTick);
	}
	
	@Override
	public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color)
	{
		getCurrentFrame().drawTexture(x, y, width, height, zDepth, partialTick, color);
	}
	
	@Override
	@Deprecated
	public ResourceLocation getTexture()
	{
		return getCurrentFrame().getTexture();
	}
	
	@Override
	@Deprecated
	public IGuiRect getBounds()
	{
		return getCurrentFrame().getBounds();
	}
	
	public IGuiTexture getCurrentFrame()
	{
		return slides[(int)Math.floor((System.currentTimeMillis()/1000D)%(slides.length * interval) / interval)];
	}
	
	public IGuiTexture[] getAllFrames()
	{
		return slides;
	}
}
