package adv_director.client.gui.editors.json.scrolling;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import adv_director.api.client.gui.GuiElement;
import adv_director.api.client.gui.lists.IScrollingEntry;
import com.mojang.realmsclient.gui.ChatFormatting;

public class ScrollingFluidGrid extends GuiElement implements IScrollingEntry
{
	private final List<FluidStack> itemList = new ArrayList<FluidStack>();
	private final Minecraft mc;
	
	private int posX = 0;
	private int posY = 0;
	private int columns = 1;
	private int rows = 1;
	
	// Necessary to prevent too much rendering inside the list
	private int upperBounds = 0;
	private int lowerBounds = 0;
	
	public ScrollingFluidGrid(int top, int bottom)
	{
		this.mc = Minecraft.getMinecraft();
		this.upperBounds = top;
		this.lowerBounds = bottom;
	}
	
	public List<FluidStack> getFluidList()
	{
		return itemList;
	}
	
	@Override
	public void drawBackground(int mx, int my, int px, int py, int width)
	{
		this.posX = px;
		this.posY = py;
		
		if(itemList.size() < 0)
		{
			return;
		}
		
		columns = Math.max(1, width/18);
		rows = MathHelper.ceiling_float_int(itemList.size()/(float)columns);
		
		int sr = (upperBounds - py)/18;
		int er = MathHelper.ceiling_float_int((lowerBounds - py)/18F);
		
		for(int j = sr; j < rows && j < er; j++)
		{
			for(int i = 0; i < columns; i++)
			{
				int idx = (j * columns) + i;
				
				if(idx >= itemList.size())
				{
					break;
				}
				
				FluidStack stack = itemList.get(idx);
				
				GlStateManager.pushMatrix();
				GlStateManager.color(1F, 1F, 1F, 1F);
				mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
				drawTexturedModalRect(px + i * 18, py + j * 18, 0, 48, 18, 18);
				GlStateManager.enableDepth();
				try
				{
					mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					TextureAtlasSprite fluidTx = mc.getTextureMapBlocks().getAtlasSprite(stack.getFluid().getStill().toString());
					fluidTx = fluidTx != null? fluidTx : mc.getTextureMapBlocks().getAtlasSprite("missingno");
					this.drawTexturedModelRectFromIcon(px + i * 18 + 1, py + j * 18 + 1, fluidTx, 16, 16);
				} catch(Exception e){}
				GlStateManager.disableDepth();
				GlStateManager.popMatrix();
			}
		}
	}
	
	@Override
	public void drawForeground(int mx, int my, int px, int py, int width)
	{
		FluidStack stack = getStackUnderMouse(mx, my);
		
		if(stack != null)
		{
			ArrayList<String> tt = new ArrayList<String>();
			tt.add(stack.getLocalizedName());
			tt.add(ChatFormatting.GRAY + "" + stack.amount + "mB");
			this.drawTooltip(tt, mx, my, mc.fontRendererObj);
		}
	}
	
	@Override
	public void onMouseClick(int mx, int my, int px, int py, int click, int index)
	{
	}
	
	public FluidStack getStackUnderMouse(int mx, int my)
	{
		int idx = mouseToIndex(mx, my);
		
		if(idx < 0 || idx >= itemList.size())
		{
			return null;
		}
		
		return itemList.get(idx);
	}
	
	private int mouseToIndex(int mx, int my)
	{
		int ii = (mx - posX)/18;
		int jj = (my - posY)/18;
		
		if(ii >= columns || jj >= rows)
		{
			return -1;
		}
		
		return (jj * columns) + ii;
	}
	
	@Override
	public int getHeight()
	{
		return rows * 18;
	}
	
	@Override
	public boolean canDrawOutsideBox(boolean isForeground)
	{
		return isForeground;
	}
}
