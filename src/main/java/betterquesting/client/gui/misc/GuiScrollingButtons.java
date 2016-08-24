package betterquesting.client.gui.misc;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.premade.GuiBQScrolling;

public class GuiScrollingButtons extends GuiBQScrolling
{
	ArrayList<GuiButton> buttons = new ArrayList<GuiButton>();
	
	public GuiScrollingButtons(int x, int y, int w, int h)
	{
		super(x, y, w, h, 20);
	}
	
	public void AddButton(GuiButton btn)
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
	        GuiButton btn = buttons.get(index);
	        btn.xPosition = left;
	        btn.yPosition = posY;
	        btn.width = listWidth - 8;
	        btn.height = h;
	        btn.drawButton(client, mouseX, mouseY);
		}
		GL11.glPopMatrix();
	}
}
