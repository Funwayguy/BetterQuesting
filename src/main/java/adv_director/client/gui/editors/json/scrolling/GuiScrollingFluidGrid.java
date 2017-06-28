package adv_director.client.gui.editors.json.scrolling;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;
import adv_director.api.client.gui.lists.GuiScrollingBase;

public class GuiScrollingFluidGrid extends GuiScrollingBase<ScrollingFluidGrid>
{
	private final int posY;
	private final int height;
	
	public GuiScrollingFluidGrid(Minecraft mc, int x, int y, int w, int h)
	{
		super(mc, x, y, w, h);
		this.posY = y;
		this.height = h;
		this.getEntryList().add(new ScrollingFluidGrid(y, y + h));
	}
	
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		super.drawBackground(mx, my, partialTick);
		
		if(this.getEntryList().size() <= 0)
		{
			this.getEntryList().add(new ScrollingFluidGrid(posY, posY + height));
		} else if(this.getEntryList().size() > 1)
		{
			while(this.getEntryList().size() > 1)
			{
				this.getEntryList().remove(1);
			}
		}
	}
	
	// Doesn't support BigItemStack
	public List<FluidStack> getFluidList()
	{
		if(this.getEntryList().size() > 0)
		{
			return this.getEntryList().get(0).getFluidList();
		}
		
		return new ArrayList<FluidStack>();
	}
	
	public FluidStack getStackUnderMouse(int mx, int my)
	{
		int idx = this.getEntryUnderMouse(mx, my);
		
		if(idx >= 0)
		{
			return this.getEntryList().get(idx).getStackUnderMouse(mx, my);
		}
		
		return null;
	}
}
