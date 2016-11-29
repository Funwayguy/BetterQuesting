package betterquesting.api.client.gui.lists;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fluids.FluidStack;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.utils.RenderUtils;
import com.mojang.realmsclient.gui.ChatFormatting;

public class GuiScrollingFluids extends GuiScrollingBase<GuiScrollingFluids.ScrollingEntryFluid>
{
	private final Minecraft mc;
	
	public GuiScrollingFluids(Minecraft mc, int x, int y, int w, int h)
	{
		super(mc, x, y, w, h);
		this.mc = mc;
		this.allowDragScroll(true);
	}
	
	public void addFluid(FluidStack stack)
	{
		String desc = stack.getLocalizedName();
		desc += "\n" + stack.amount + "mB";
		addFluid(stack, desc);
	}
	
	public void addFluid(FluidStack stack, String description)
	{
		this.getEntryList().add(new ScrollingEntryFluid(mc, stack, description));
	}
	
	public static class ScrollingEntryFluid extends GuiElement implements IScrollingEntry
	{
		private final Minecraft mc;
		private FluidStack stack;
		private String desc = "";
		
		public ScrollingEntryFluid(Minecraft mc, FluidStack stack, String desc)
		{
			this.mc = mc;
			this.stack = stack;
			
			this.setDescription(desc);
		}
		
		public void setDescription(String desc)
		{
			this.desc = desc == null? "" : desc;
		}
		
		@Override
		public void drawBackground(int mx, int my, int px, int py, int width)
		{
			GlStateManager.pushMatrix();
			
			RenderUtils.DrawLine(px, py, px + width, py, 1F, getTextColor());
			
			GlStateManager.color(1F, 1F, 1F, 1F);
			
			GlStateManager.translate(px, py, 0F);
			GlStateManager.scale(2F, 2F, 2F);
			
			this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
			this.drawTexturedModalRect(0, 0, 0, 48, 18, 18);
			
			GlStateManager.enableDepth();
			this.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			
			if(stack != null)
			{
				try
				{
					TextureAtlasSprite fluidTx = mc.getTextureMapBlocks().getAtlasSprite(stack.getFluid().getStill().toString());
					fluidTx = fluidTx != null? fluidTx : mc.getTextureMapBlocks().getAtlasSprite("missingno");
					this.drawTexturedModelRectFromIcon(1, 1, fluidTx, 16, 16);
				} catch(Exception e){}
			}
			
			GlStateManager.disableDepth();
			
			GlStateManager.popMatrix();
			
			RenderUtils.drawSplitString(mc.fontRendererObj, desc, px + 40, py + 4, width - 40, getTextColor(), false, 0, 2);
		}
		
		@Override
		public void drawForeground(int mx, int my, int px, int py, int width)
		{
			if(stack != null && isWithin(mx, my, px + 2, py + 2, 32, 32))
			{
				try
				{
					List<String> tt = new ArrayList<String>();
					tt.add(stack.getLocalizedName());
					tt.add(ChatFormatting.GRAY + "" + stack.amount + "mB");
					this.drawTooltip(tt, mx, my, mc.fontRendererObj);
				} catch(Exception e){}
			}
		}
		
		@Override
		public void onMouseClick(int mx, int my, int px, int py, int click, int index)
		{
			// JEI/NEI support here
		}

		@Override
		public int getHeight()
		{
			return 36;
		}

		@Override
		public boolean canDrawOutsideBox(boolean isForeground)
		{
			return isForeground;
		}
	}
}
