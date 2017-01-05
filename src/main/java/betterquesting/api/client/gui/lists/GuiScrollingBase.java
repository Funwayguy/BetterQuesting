package betterquesting.api.client.gui.lists;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.utils.RenderUtils;

@SideOnly(Side.CLIENT)
public abstract class GuiScrollingBase<T extends IScrollingEntry> extends GuiElement implements IGuiEmbedded
{
	private List<T> entries = new ArrayList<T>();
	
	private final Minecraft mc;
	private int scroll = 0;
	private int posX = 0;
	private int posY = 0;
	private int width = 0;
	private int height = 0;
	private boolean dragScroll = false;
	private boolean hideBounds = false;
	
	private int dragState = -1;
	private int myDrag = 0;
	private int scrollDrag = 0;
	
	public GuiScrollingBase(Minecraft mc, int x, int y, int w, int h)
	{
		this.mc = mc;
		this.posX = x;
		this.posY = y;
		this.width = w;
		this.height = h;
	}
	
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		int listY = posY - scroll;
		int maxScroll = Math.max(0, getListHeight() - height);
		
		boolean isHovering = isWithin(mx, my, posX, posY, width - 8, height);
		int mx2 = !isHovering? -256 : mx;
		int my2 = !isHovering? -256 : my;
		
		if(maxScroll > 0 && (Mouse.isButtonDown(0) || Mouse.isButtonDown(2)))
		{
			if(dragScroll && (dragState == 0 || (dragState == -1 && isHovering)))
			{
				this.setScrollPos(scrollDrag - (my - myDrag));
				dragState = 0; // Manual drag
			} else if(dragState == 1 || (dragState == -1 && isWithin(mx, my, posX + width - 8, posY, 8, height)))
			{
				this.setScrollPos((int)((my - posY - 8)/(float)(height - 20F) * maxScroll));
				dragState = 1; // Scroll bar
			} else
			{
				dragState = 3; // Missed
			}
		} else
		{
			dragState = -1; // Idle
			scrollDrag = scroll;
			myDrag = my;
		}
		
		scroll = MathHelper.clamp_int(scroll, 0, maxScroll);
		
		GL11.glPushMatrix();
		
		for(int i = 0; i < entries.size(); i++)
		{
			IScrollingEntry e = entries.get(i);
			boolean scissor = !e.canDrawOutsideBox(false);
			
			if(scissor)
			{
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				RenderUtils.guiScissor(mc, posX, posY, width - 8, height);
			}
			
			e.drawBackground(mx2, my2, posX, listY, width - 8);
			
			if(scissor)
			{
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
			}
			
			listY += e.getHeight();
		}
		
		if(!hideBounds)
		{
			RenderUtils.DrawLine(posX, posY, posX + width - 8, posY, 1F, getTextColor());
			RenderUtils.DrawLine(posX, posY + height, posX + width - 8, posY + height, 1F, getTextColor());
		}
		
		this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		if(maxScroll > 0)
		{
			this.drawTexturedModalRect(posX + width - 8, posY, 248, 0, 8, 20);
			int n = 20;
			while(n + 20 < height)
			{
				this.drawTexturedModalRect(posX + width - 8, posY + n, 248, 20, 8, 20);
				n += 20;
			}
			
			if(height%20 != 0)
			{
				// Little bit of trickery here to neaten things up
				n -= 20 - height%20;
			}
			this.drawTexturedModalRect(posX + width - 8, posY + n, 248, 40, 8, 20);
			this.drawTexturedModalRect(posX + width - 8, posY + (int)Math.max(0, n * (float)scroll/(float)maxScroll), 248, 60, 8, 20);
		}
		
		GL11.glPopMatrix();
	}
	
	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
		int count = entries.size();
		int listY = posY - scroll;
		boolean isHovering = isWithin(mx, my, posX, posY, width, height);
		int mx2 = !isHovering? -99 : mx;
		int my2 = !isHovering? -99 : my;
		
		GL11.glPushMatrix();
		
		for(int i = 0; i < count; i++)
		{
			IScrollingEntry e = entries.get(i);
			boolean scissor = !e.canDrawOutsideBox(true);
			
			if(scissor)
			{
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				RenderUtils.guiScissor(mc, posX, posY, width - 8, height);
			}
			
			e.drawForeground(mx2, my2, posX, listY, width - 8);
			
			if(scissor)
			{
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
			}
			
			listY += e.getHeight();
		}
		
		GL11.glPopMatrix();
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		int listY = posY - scroll;
		
		for(int i = entries.size() - 1; i >= 0; i--)
		{
			IScrollingEntry e = entries.get(i);
			e.onMouseClick(mx, my, posX, listY, click, i);
			listY += e.getHeight();
		}
	}
	
	@Override
	public void onMouseScroll(int mx, int my, int dx)
	{
		if(isWithin(mx, my, posX, posY, width, height))
		{
			int maxScroll = Math.max(0, getListHeight() - height);
			
			scroll = MathHelper.clamp_int(scroll + (dx * getScrollSpeed()), 0, maxScroll);
		}
	}
	
	@Override
	public void onKeyTyped(char c, int keyCode)
	{
	}
	
	public void allowDragScroll(boolean state)
	{
		this.dragScroll = state;
	}
	
	public void hideBounds(boolean state)
	{
		this.hideBounds = state;
	}
	
	public int getEntryUnderMouse(int mx, int my)
	{
		if(!isWithin(mx, my, posX, posY, width - 8, height))
		{
			return -1;
		}
		
		int listY = posY - scroll;
		
		for(int i = 0; i < entries.size(); i++)
		{
			if(listY > posY + height)
			{
				break;
			}
			
			IScrollingEntry e = entries.get(i);
			
			if(my >= listY && my < listY + e.getHeight())
			{
				return i;
			}
			
			listY += e.getHeight();
		}
		
		return -1;
	}
	
	public int getListHeight()
	{
		int n = 0;
		
		for(IScrollingEntry e : getEntryList())
		{
			n += e.getHeight();
		}
		
		return n;
	}
	
	public int getListWidth()
	{
		return width - 8;
	}
	
	public List<T> getEntryList()
	{
		return entries;
	}
	
	public int getScrollSpeed()
	{
		return 8;
	}
	
	public void setScrollPos(int value)
	{
		int maxScroll = Math.max(0, getListHeight() - height);
		scroll = MathHelper.clamp_int(value, 0, maxScroll);
	}
}
