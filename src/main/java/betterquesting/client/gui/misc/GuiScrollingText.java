package betterquesting.client.gui.misc;

import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.RenderUtils;

/**
 * Creates a scrolling region of split text. Line spacing is slightly larger for better readability
 */
public class GuiScrollingText extends GuiBQScrolling
{
	GuiScreen parent;
	String rawText = "";
	List<String> text;

	public GuiScrollingText(GuiScreen parent, int width, int height, int top, int left)
	{
		this(parent, width, height, top, left, "");
	}
	
	public GuiScrollingText(GuiScreen parent, int width, int height, int top, int left, String text)
	{
		super(parent.mc, width, height, top, top + height, left, parent.mc.fontRenderer.FONT_HEIGHT + 2);
		this.parent = parent;
		this.SetText(text);
	}
	
	@SuppressWarnings("unchecked")
	public void SetText(String txt)
	{
		this.rawText = txt.replaceAll("\r", "");
		this.text = parent.mc.fontRenderer.listFormattedStringToWidth(rawText, listWidth - 12);
	}
	
	public String getText()
	{
		return rawText;
	}

	@Override
	protected int getSize()
	{
		return text.size();
	}
	
	@Override
	protected void elementClicked(int index, boolean doubleClick)
	{
	}

    @Override
    protected int getContentHeight()
    {
    	return text.size() * (parent.mc.fontRenderer.FONT_HEIGHT + 2);
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
		if(posY + 1 < top || posY + slotHeight - 1 > bottom)
		{
			return;
		}
		
		// Using this to preserve color formatting
		RenderUtils.drawSplitString(parent.mc.fontRenderer, rawText, left + 4, posY + 1, listWidth - 12, ThemeRegistry.curTheme().textColor().getRGB(), false, index, index);
	}
}
