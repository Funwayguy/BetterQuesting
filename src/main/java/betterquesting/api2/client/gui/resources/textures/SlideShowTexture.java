package betterquesting.api2.client.gui.resources.textures;

import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.IGuiRect;

public class SlideShowTexture implements IGuiTexture
{
	private final IGuiTexture[] slides;
	private final float interval;
	
	public SlideShowTexture(IGuiTexture[] slides, float interval)
	{
		this.slides = slides;
		this.interval = interval;
	}
	
	@Override
	public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick)
	{
		IGuiTexture gTx = getCurrentFrame();
		
		gTx.drawTexture(x, y, width, height, zDepth, partialTick);
	}
	
	public IGuiTexture getCurrentFrame()
	{
		float animL = slides.length * interval;
		
		int frame = (int)Math.floor((System.currentTimeMillis()/1000D)%animL / interval);
		
		return slides[frame];
	}
	
	@Override
	@Deprecated
	public ResourceLocation getTexture()
	{
		float animL = slides.length * interval;
		
		int frame = (int)Math.floor((System.currentTimeMillis()/1000D)%animL / interval);
		
		return getAllTextures()[frame];
	}
	
	public ResourceLocation[] getAllTextures()
	{
		ResourceLocation[] txAry = new ResourceLocation[slides.length];
		
		for(int i = 0; i < slides.length; i++)
		{
			txAry[i] = slides[i].getTexture();
		}
		
		return txAry;
	}
	
	@Override
	@Deprecated
	public IGuiRect getBounds()
	{
		float animL = slides.length * interval;
		
		int frame = (int)Math.floor((System.currentTimeMillis()/1000D)%animL / interval);
		
		return getAllBounds()[frame];
	}
	
	public IGuiRect[] getAllBounds()
	{
		IGuiRect[] txAry = new IGuiRect[slides.length];
		
		for(int i = 0; i < slides.length; i++)
		{
			txAry[i] = slides[i].getBounds();
		}
		
		return txAry;
	}
	
	public int getSlideCount()
	{
		return slides.length;
	}
}
