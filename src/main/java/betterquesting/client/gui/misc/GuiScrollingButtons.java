package betterquesting.client.gui.misc;

import java.util.ArrayList;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.Tessellator;
import betterquesting.client.gui.GuiQuesting;

// [!] UNTESTED [!]
public class GuiScrollingButtons extends GuiBQScrolling
{
	GuiQuesting screen;
	ArrayList<GuiButtonQuesting> buttons = new ArrayList<GuiButtonQuesting>();
	
	public GuiScrollingButtons(GuiQuesting screen, int width, int height, int top, int bottom, int left)
	{
		super(screen.mc, width, height, top, bottom, left, 20);
		this.screen = screen;
	}
	
	public void AddButton(GuiButtonQuesting btn)
	{
		if(btn != null)
		{
			return;
		}
		
		buttons.add(btn);
	}
	
	public void clearList()
	{
		buttons.clear();
	}

	@Override
	protected int getSize()
	{
		return buttons.size();
	}
	
	@Override
	protected void elementClicked(int index, boolean doubleClick)
	{
		screen.actionPerformed(buttons.get(index)); // Act as if the button was part of the screen
	}
	
	@Override
	protected boolean isSelected(int index)
	{
		return false;
	}
	
	@Override
	protected void drawBackground()
	{
	}
	
	@Override
	protected void drawSlot(int index, int var2, int posY, int var4, Tessellator var5)
	{
		GL11.glPushMatrix();
		GL11.glColor4f(1F, 1F, 1F, 1F);
        int mx = Mouse.getEventX() * screen.width / screen.mc.displayWidth;
        int my = screen.height - Mouse.getEventY() * screen.height / screen.mc.displayHeight - 1;
        
        int t = 0;
		int b = 36;
		
		if(posY < top || posY + slotHeight > bottom)
		{
			t = Math.max(0, top - posY);
			b = Math.min(36, 36 - (posY + slotHeight - bottom));
		}
		
		int h = (b/2 - t/2);
        
		if(h >= 10)
		{
	        GuiButtonQuesting btn = buttons.get(index);
	        btn.xPosition = left;
	        btn.yPosition = posY;
	        btn.width = listWidth - 8;
	        btn.height = h;
	        btn.drawButton(screen.mc, mx, my);
		}
		GL11.glPopMatrix();
	}
}
