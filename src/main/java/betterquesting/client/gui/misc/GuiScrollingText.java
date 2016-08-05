package betterquesting.client.gui.misc;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.client.gui.premade.GuiBQScrolling;
import betterquesting.api.utils.RenderUtils;

/**
 * Creates a scrolling region of split text. Line spacing is slightly larger for better readability
 */
public class GuiScrollingText extends GuiBQScrolling
{
	private FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
	private String rawText = "";
	private List<String> text;

	public GuiScrollingText(int x, int y, int width, int height)
	{
		this(x, y, width, height, "");
	}
	
	public GuiScrollingText(int x, int y, int width, int height, String text)
	{
		super(x, y, width, height, Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 2);
		this.SetText(text);
	}
	
	@SuppressWarnings("unchecked")
	public void SetText(String txt)
	{
		this.rawText = txt.replaceAll("\r", "");
		this.text = fontRenderer.listFormattedStringToWidth(rawText, listWidth - 12);
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
    	return text.size() * (fontRenderer.FONT_HEIGHT + 2);
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
		RenderUtils.drawSplitString(fontRenderer, rawText, left + 4, posY + 1, listWidth - 12, ExpansionAPI.INSTANCE.getThemeRegistry().getCurrentTheme().getTextColor().getRGB(), false, index, index);
	}
}
