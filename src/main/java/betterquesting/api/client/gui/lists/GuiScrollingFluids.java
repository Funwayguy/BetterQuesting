package betterquesting.api.client.gui.lists;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
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
			GL11.glPushMatrix();
			
			RenderUtils.DrawLine(px, py, px + width, py, 1F, getTextColor());
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			GL11.glTranslatef(px, py, 0F);
			GL11.glScalef(2F, 2F, 2F);
			
			this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
			this.drawTexturedModalRect(0, 0, 0, 48, 18, 18);
			
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			
			if(stack != null)
			{
				try
				{
					if(stack.getFluid().getIcon() != null)
					{
						RenderUtils.itemRender.renderIcon(1, 1, stack.getFluid().getIcon(), 16, 16);
					} else
					{
			            IIcon missing = ((TextureMap)mc.renderEngine.getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
						RenderUtils.itemRender.renderIcon(1, 1, missing, 16, 16);
					}
				} catch(Exception e){}
			}
			
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			GL11.glPopMatrix();
			
			RenderUtils.drawSplitString(mc.fontRenderer, desc, px + 40, py + 4, width - 40, getTextColor(), false, 0, 2);
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
					this.drawTooltip(tt, mx, my, mc.fontRenderer);
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
